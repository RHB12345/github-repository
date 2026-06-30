package com.qihang.campusmarket.service;

import com.qihang.campusmarket.dto.OrderView;
import com.qihang.campusmarket.entity.Product;
import com.qihang.campusmarket.entity.TradeOrder;
import com.qihang.campusmarket.entity.User;
import com.qihang.campusmarket.mapper.OrderMapper;
import com.qihang.campusmarket.mapper.ProductMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderService {
    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;

    public OrderService(OrderMapper orderMapper, ProductMapper productMapper) {
        this.orderMapper = orderMapper;
        this.productMapper = productMapper;
    }

    @CacheEvict(value = {"productSearch", "dashboardStats", "categoryStats", "productStatusStats", "campusStats", "orderStats"}, allEntries = true)
    @Transactional
    public TradeOrder reserve(Long productId, User buyer, String message) {
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("商品不存在或已下架");
        }
        if (product.getSellerId().equals(buyer.getId())) {
            throw new IllegalArgumentException("不能预订自己发布的商品");
        }
        if (!"ON_SALE".equals(product.getStatus())) {
            throw new IllegalArgumentException("该商品当前不可预订");
        }
        if (orderMapper.countActiveByBuyerAndProduct(buyer.getId(), productId) > 0) {
            throw new IllegalArgumentException("你已经提交过该商品的预订");
        }

        TradeOrder order = new TradeOrder();
        order.setOrderNo(generateOrderNo());
        order.setProductId(productId);
        order.setBuyerId(buyer.getId());
        order.setSellerId(product.getSellerId());
        order.setStatus("PENDING");
        order.setMessage(StringUtils.hasText(message) ? message.trim() : "希望尽快面交");
        orderMapper.insert(order);
        productMapper.updateStatus(productId, "RESERVED");
        return order;
    }

    public List<OrderView> buyerOrders(Long buyerId) {
        return orderMapper.findBuyerOrders(buyerId);
    }

    public List<OrderView> sellerOrders(Long sellerId) {
        return orderMapper.findSellerOrders(sellerId);
    }

    @CacheEvict(value = {"productSearch", "dashboardStats", "categoryStats", "productStatusStats", "campusStats", "orderStats"}, allEntries = true)
    @Transactional
    public void updateStatus(Long orderId, User currentUser, String targetStatus) {
        TradeOrder order = orderMapper.findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        boolean isBuyer = order.getBuyerId().equals(currentUser.getId());
        boolean isSeller = order.getSellerId().equals(currentUser.getId());
        boolean isAdmin = "ADMIN".equals(currentUser.getRole());
        if (!isBuyer && !isSeller && !isAdmin) {
            throw new IllegalArgumentException("无权操作该订单");
        }
        if ("CONFIRMED".equals(targetStatus) && !(isSeller || isAdmin)) {
            throw new IllegalArgumentException("只有卖家可以确认订单");
        }
        if ("COMPLETED".equals(targetStatus) && !(isBuyer || isAdmin)) {
            throw new IllegalArgumentException("只有买家可以确认完成交易");
        }
        if (!List.of("CONFIRMED", "COMPLETED", "CANCELED").contains(targetStatus)) {
            throw new IllegalArgumentException("不支持的订单状态");
        }

        orderMapper.updateStatus(orderId, targetStatus);
        if ("COMPLETED".equals(targetStatus)) {
            productMapper.updateStatus(order.getProductId(), "SOLD");
        } else if ("CANCELED".equals(targetStatus)) {
            productMapper.updateStatus(order.getProductId(), "ON_SALE");
        } else if ("CONFIRMED".equals(targetStatus)) {
            productMapper.updateStatus(order.getProductId(), "RESERVED");
        }
    }

    private String generateOrderNo() {
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int tail = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "QH" + stamp + tail;
    }
}
