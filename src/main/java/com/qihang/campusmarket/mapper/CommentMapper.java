package com.qihang.campusmarket.mapper;

import com.qihang.campusmarket.dto.CommentView;
import com.qihang.campusmarket.entity.Comment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper {

    @Insert("INSERT INTO comments(product_id, user_id, content) VALUES(#{productId}, #{userId}, #{content})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Comment comment);

    @Select("""
            SELECT c.id, c.product_id, c.user_id, c.content, c.created_at, u.nickname, u.avatar_url
            FROM comments c
            LEFT JOIN users u ON u.id = c.user_id
            WHERE c.product_id = #{productId}
            ORDER BY c.created_at DESC
            """)
    List<CommentView> findByProductId(Long productId);
}
