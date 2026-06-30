package com.qihang.campusmarket.mapper;

import com.qihang.campusmarket.dto.OrderView;
import com.qihang.campusmarket.entity.TradeOrder;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface OrderMapper {

    @Insert("""
            INSERT INTO trade_orders(order_no, product_id, buyer_id, seller_id, status, message)
            VALUES(#{orderNo}, #{productId}, #{buyerId}, #{sellerId}, #{status}, #{message})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TradeOrder order);

    @Select("SELECT * FROM trade_orders WHERE id = #{id}")
    TradeOrder findById(Long id);

    @Select("""
            SELECT COUNT(*)
            FROM trade_orders
            WHERE product_id = #{productId}
              AND buyer_id = #{buyerId}
              AND status IN ('PENDING', 'CONFIRMED')
            """)
    int countActiveByBuyerAndProduct(@Param("buyerId") Long buyerId, @Param("productId") Long productId);

    @Select("""
            SELECT
                o.id, o.order_no, o.product_id, p.title AS product_title,
                COALESCE((SELECT pi.url FROM product_images pi WHERE pi.product_id = p.id ORDER BY pi.sort_no ASC, pi.id ASC LIMIT 1),
                         '/images/products/other.svg') AS cover_url,
                p.price, o.buyer_id, buyer.nickname AS buyer_name,
                o.seller_id, seller.nickname AS seller_name,
                o.status, o.message, o.created_at, o.completed_at
            FROM trade_orders o
            INNER JOIN products p ON p.id = o.product_id
            LEFT JOIN users buyer ON buyer.id = o.buyer_id
            LEFT JOIN users seller ON seller.id = o.seller_id
            WHERE o.buyer_id = #{buyerId}
            ORDER BY o.created_at DESC
            """)
    List<OrderView> findBuyerOrders(Long buyerId);

    @Select("""
            SELECT
                o.id, o.order_no, o.product_id, p.title AS product_title,
                COALESCE((SELECT pi.url FROM product_images pi WHERE pi.product_id = p.id ORDER BY pi.sort_no ASC, pi.id ASC LIMIT 1),
                         '/images/products/other.svg') AS cover_url,
                p.price, o.buyer_id, buyer.nickname AS buyer_name,
                o.seller_id, seller.nickname AS seller_name,
                o.status, o.message, o.created_at, o.completed_at
            FROM trade_orders o
            INNER JOIN products p ON p.id = o.product_id
            LEFT JOIN users buyer ON buyer.id = o.buyer_id
            LEFT JOIN users seller ON seller.id = o.seller_id
            WHERE o.seller_id = #{sellerId}
            ORDER BY o.created_at DESC
            """)
    List<OrderView> findSellerOrders(Long sellerId);

    @Select("""
            SELECT
                o.id, o.order_no, o.product_id, p.title AS product_title,
                COALESCE((SELECT pi.url FROM product_images pi WHERE pi.product_id = p.id ORDER BY pi.sort_no ASC, pi.id ASC LIMIT 1),
                         '/images/products/other.svg') AS cover_url,
                p.price, o.buyer_id, buyer.nickname AS buyer_name,
                o.seller_id, seller.nickname AS seller_name,
                o.status, o.message, o.created_at, o.completed_at
            FROM trade_orders o
            INNER JOIN products p ON p.id = o.product_id
            LEFT JOIN users buyer ON buyer.id = o.buyer_id
            LEFT JOIN users seller ON seller.id = o.seller_id
            ORDER BY o.created_at DESC
            LIMIT #{limit}
            """)
    List<OrderView> findLatest(@Param("limit") int limit);

    @Update("""
            UPDATE trade_orders
            SET status = #{status},
                completed_at = CASE WHEN #{status} = 'COMPLETED' THEN CURRENT_TIMESTAMP ELSE completed_at END,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = #{id}
            """)
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
