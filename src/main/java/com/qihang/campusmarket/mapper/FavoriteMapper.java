package com.qihang.campusmarket.mapper;

import com.qihang.campusmarket.dto.ProductCard;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FavoriteMapper {

    @Insert("INSERT INTO favorites(user_id, product_id) VALUES(#{userId}, #{productId})")
    int insert(@Param("userId") Long userId, @Param("productId") Long productId);

    @Delete("DELETE FROM favorites WHERE user_id = #{userId} AND product_id = #{productId}")
    int delete(@Param("userId") Long userId, @Param("productId") Long productId);

    @Select("SELECT COUNT(*) FROM favorites WHERE user_id = #{userId} AND product_id = #{productId}")
    int exists(@Param("userId") Long userId, @Param("productId") Long productId);

    @Select("SELECT COUNT(*) FROM favorites WHERE product_id = #{productId}")
    int countByProduct(Long productId);

    @Select("""
            SELECT
                p.id, p.title, p.category, p.price, p.condition_label, p.description, p.status,
                p.campus_area, p.trade_place, p.view_count, p.created_at,
                COALESCE((SELECT pi.url FROM product_images pi WHERE pi.product_id = p.id ORDER BY pi.sort_no ASC, pi.id ASC LIMIT 1),
                         '/images/products/other.svg') AS cover_url,
                u.nickname AS seller_nickname,
                u.campus AS seller_campus,
                (SELECT COUNT(*) FROM favorites ff WHERE ff.product_id = p.id) AS favorite_count
            FROM favorites f
            INNER JOIN products p ON p.id = f.product_id
            LEFT JOIN users u ON u.id = p.seller_id
            WHERE f.user_id = #{userId} AND p.deleted = FALSE
            ORDER BY f.created_at DESC
            """)
    List<ProductCard> findProductsByUser(Long userId);
}
