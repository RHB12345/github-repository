package com.qihang.campusmarket.mapper;

import com.qihang.campusmarket.dto.ProductCard;
import com.qihang.campusmarket.entity.Product;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ProductMapper {

    @Insert("""
            INSERT INTO products(seller_id, title, category, price, condition_label, description, status, campus_area, trade_place, view_count, deleted)
            VALUES(#{sellerId}, #{title}, #{category}, #{price}, #{conditionLabel}, #{description}, #{status}, #{campusArea}, #{tradePlace}, #{viewCount}, #{deleted})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);

    @Select("SELECT * FROM products WHERE id = #{id} AND deleted = FALSE")
    Product findById(Long id);

    @Select("""
            <script>
            SELECT
                p.id, p.title, p.category, p.price, p.condition_label, p.description, p.status,
                p.campus_area, p.trade_place, p.view_count, p.created_at,
                COALESCE((SELECT pi.url FROM product_images pi WHERE pi.product_id = p.id ORDER BY pi.sort_no ASC, pi.id ASC LIMIT 1),
                         CASE p.category
                             WHEN '教材资料' THEN '/images/products/book.svg'
                             WHEN '数码电子' THEN '/images/products/device.svg'
                             WHEN '生活用品' THEN '/images/products/life.svg'
                             WHEN '运动户外' THEN '/images/products/sport.svg'
                             ELSE '/images/products/other.svg'
                         END) AS cover_url,
                u.nickname AS seller_nickname,
                u.campus AS seller_campus,
                (SELECT COUNT(*) FROM favorites f WHERE f.product_id = p.id) AS favorite_count
            FROM products p
            LEFT JOIN users u ON u.id = p.seller_id
            WHERE p.deleted = FALSE
            <if test="status != null and status != ''">
                AND p.status = #{status}
            </if>
            <if test="category != null and category != ''">
                AND p.category = #{category}
            </if>
            <if test="searchTerms != null and searchTerms.size() > 0">
                AND (
                <foreach collection="searchTerms" item="term" separator=" OR ">
                    (p.title LIKE CONCAT('%', #{term}, '%')
                     OR p.description LIKE CONCAT('%', #{term}, '%')
                     OR p.category LIKE CONCAT('%', #{term}, '%'))
                </foreach>
                )
            </if>
            <choose>
                <when test="sort == 'priceAsc'">ORDER BY p.price ASC, p.created_at DESC</when>
                <when test="sort == 'priceDesc'">ORDER BY p.price DESC, p.created_at DESC</when>
                <when test="sort == 'hot'">ORDER BY favorite_count DESC, p.view_count DESC, p.created_at DESC</when>
                <otherwise>ORDER BY p.created_at DESC</otherwise>
            </choose>
            LIMIT #{size} OFFSET #{offset}
            </script>
            """)
    List<ProductCard> search(@Param("searchTerms") List<String> searchTerms,
                             @Param("category") String category,
                             @Param("status") String status,
                             @Param("sort") String sort,
                             @Param("offset") int offset,
                             @Param("size") int size);

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM products p
            WHERE p.deleted = FALSE
            <if test="status != null and status != ''">
                AND p.status = #{status}
            </if>
            <if test="category != null and category != ''">
                AND p.category = #{category}
            </if>
            <if test="searchTerms != null and searchTerms.size() > 0">
                AND (
                <foreach collection="searchTerms" item="term" separator=" OR ">
                    (p.title LIKE CONCAT('%', #{term}, '%')
                     OR p.description LIKE CONCAT('%', #{term}, '%')
                     OR p.category LIKE CONCAT('%', #{term}, '%'))
                </foreach>
                )
            </if>
            </script>
            """)
    long countSearch(@Param("searchTerms") List<String> searchTerms,
                     @Param("category") String category,
                     @Param("status") String status);

    @Select("""
            SELECT
                p.id, p.title, p.category, p.price, p.condition_label, p.description, p.status,
                p.campus_area, p.trade_place, p.view_count, p.created_at,
                COALESCE((SELECT pi.url FROM product_images pi WHERE pi.product_id = p.id ORDER BY pi.sort_no ASC, pi.id ASC LIMIT 1),
                         '/images/products/other.svg') AS cover_url,
                u.nickname AS seller_nickname,
                u.campus AS seller_campus,
                (SELECT COUNT(*) FROM favorites f WHERE f.product_id = p.id) AS favorite_count
            FROM products p
            LEFT JOIN users u ON u.id = p.seller_id
            WHERE p.seller_id = #{sellerId} AND p.deleted = FALSE
            ORDER BY p.created_at DESC
            """)
    List<ProductCard> findBySellerId(Long sellerId);

    @Select("""
            <script>
            SELECT
                p.id, p.title, p.category, p.price, p.condition_label, p.description, p.status,
                p.campus_area, p.trade_place, p.view_count, p.created_at,
                COALESCE((SELECT pi.url FROM product_images pi WHERE pi.product_id = p.id ORDER BY pi.sort_no ASC, pi.id ASC LIMIT 1),
                         '/images/products/other.svg') AS cover_url,
                u.nickname AS seller_nickname,
                u.campus AS seller_campus,
                (SELECT COUNT(*) FROM favorites f WHERE f.product_id = p.id) AS favorite_count
            FROM products p
            LEFT JOIN users u ON u.id = p.seller_id
            WHERE p.deleted = FALSE
              AND p.status = 'ON_SALE'
              AND p.id != #{excludeId}
            <if test="category != null and category != ''">
              AND p.category = #{category}
            </if>
            ORDER BY favorite_count DESC, p.view_count DESC, p.created_at DESC
            LIMIT #{limit}
            </script>
            """)
    List<ProductCard> findSimilar(@Param("category") String category,
                                  @Param("excludeId") Long excludeId,
                                  @Param("limit") int limit);

    @Select("""
            SELECT AVG(price)
            FROM products
            WHERE deleted = FALSE
              AND category = #{category}
              AND status IN ('ON_SALE', 'RESERVED', 'SOLD')
            """)
    java.math.BigDecimal averagePriceByCategory(String category);

    @Update("UPDATE products SET view_count = view_count + 1 WHERE id = #{id}")
    int increaseView(Long id);

    @Update("""
            UPDATE products
            SET status = #{status},
                updated_at = CURRENT_TIMESTAMP
            WHERE id = #{id}
            """)
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Update("""
            UPDATE products
            SET deleted = TRUE,
                status = 'OFFLINE',
                updated_at = CURRENT_TIMESTAMP
            WHERE id = #{id} AND seller_id = #{sellerId}
            """)
    int softDelete(@Param("id") Long id, @Param("sellerId") Long sellerId);
}
