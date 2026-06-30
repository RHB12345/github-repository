package com.qihang.campusmarket.mapper;

import com.qihang.campusmarket.dto.StatItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DashboardMapper {

    @Select("SELECT COUNT(*) FROM users")
    long countUsers();

    @Select("SELECT COUNT(*) FROM products WHERE deleted = FALSE")
    long countProducts();

    @Select("SELECT COUNT(*) FROM trade_orders")
    long countOrders();

    @Select("SELECT COUNT(*) FROM trade_orders WHERE status = 'COMPLETED'")
    long countTurnovers();

    @Select("""
            SELECT category AS label, COUNT(*) AS `value`
            FROM products
            WHERE deleted = FALSE
            GROUP BY category
            ORDER BY `value` DESC
            """)
    List<StatItem> productCategoryStats();

    @Select("""
            SELECT status AS label, COUNT(*) AS `value`
            FROM products
            WHERE deleted = FALSE
            GROUP BY status
            ORDER BY `value` DESC
            """)
    List<StatItem> productStatusStats();

    @Select("""
            SELECT campus_area AS label, COUNT(*) AS `value`
            FROM products
            WHERE deleted = FALSE
            GROUP BY campus_area
            ORDER BY `value` DESC
            """)
    List<StatItem> campusProductStats();

    @Select("""
            SELECT status AS label, COUNT(*) AS `value`
            FROM trade_orders
            GROUP BY status
            ORDER BY `value` DESC
            """)
    List<StatItem> orderStatusStats();
}
