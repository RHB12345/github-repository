package com.qihang.campusmarket.mapper;

import com.qihang.campusmarket.entity.ProductImage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductImageMapper {

    @Insert("""
            INSERT INTO product_images(product_id, url, sort_no)
            VALUES(#{productId}, #{url}, #{sortNo})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ProductImage image);

    @Select("SELECT * FROM product_images WHERE product_id = #{productId} ORDER BY sort_no ASC, id ASC")
    List<ProductImage> findByProductId(Long productId);

    @Select("""
            SELECT COALESCE((SELECT url FROM product_images WHERE product_id = #{productId} ORDER BY sort_no ASC, id ASC LIMIT 1), #{fallback})
            """)
    String findCover(@Param("productId") Long productId, @Param("fallback") String fallback);
}
