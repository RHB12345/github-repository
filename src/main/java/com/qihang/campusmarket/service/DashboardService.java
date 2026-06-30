package com.qihang.campusmarket.service;

import com.qihang.campusmarket.dto.DashboardStats;
import com.qihang.campusmarket.dto.MessageView;
import com.qihang.campusmarket.dto.OrderView;
import com.qihang.campusmarket.dto.StatItem;
import com.qihang.campusmarket.entity.User;
import com.qihang.campusmarket.mapper.DashboardMapper;
import com.qihang.campusmarket.mapper.MessageMapper;
import com.qihang.campusmarket.mapper.OrderMapper;
import com.qihang.campusmarket.mapper.UserMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {
    private final DashboardMapper dashboardMapper;
    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final MessageMapper messageMapper;

    public DashboardService(DashboardMapper dashboardMapper,
                            UserMapper userMapper,
                            OrderMapper orderMapper,
                            MessageMapper messageMapper) {
        this.dashboardMapper = dashboardMapper;
        this.userMapper = userMapper;
        this.orderMapper = orderMapper;
        this.messageMapper = messageMapper;
    }

    @Cacheable("dashboardStats")
    public DashboardStats overview() {
        DashboardStats stats = new DashboardStats();
        stats.setUserCount(dashboardMapper.countUsers());
        stats.setProductCount(dashboardMapper.countProducts());
        stats.setOrderCount(dashboardMapper.countOrders());
        stats.setTurnoverCount(dashboardMapper.countTurnovers());
        return stats;
    }

    @Cacheable("categoryStats")
    public List<StatItem> categoryStats() {
        return dashboardMapper.productCategoryStats();
    }

    @Cacheable("productStatusStats")
    public List<StatItem> productStatusStats() {
        return dashboardMapper.productStatusStats();
    }

    @Cacheable("campusStats")
    public List<StatItem> campusStats() {
        return dashboardMapper.campusProductStats();
    }

    @Cacheable("orderStats")
    public List<StatItem> orderStats() {
        return dashboardMapper.orderStatusStats();
    }

    public List<User> latestUsers(int limit) {
        return userMapper.findLatest(Math.max(1, Math.min(limit, 12)));
    }

    public List<OrderView> latestOrders(int limit) {
        return orderMapper.findLatest(Math.max(1, Math.min(limit, 12)));
    }

    public List<MessageView> latestMessages(int limit) {
        return messageMapper.findLatest(Math.max(1, Math.min(limit, 12)));
    }
}
