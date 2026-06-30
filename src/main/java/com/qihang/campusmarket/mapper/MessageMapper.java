package com.qihang.campusmarket.mapper;

import com.qihang.campusmarket.dto.MessageView;
import com.qihang.campusmarket.entity.Message;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MessageMapper {

    @Insert("""
            INSERT INTO messages(sender_id, receiver_id, product_id, content, read_flag)
            VALUES(#{senderId}, #{receiverId}, #{productId}, #{content}, #{readFlag})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Message message);

    @Select("""
            SELECT
                m.id, m.sender_id, sender.nickname AS sender_name,
                m.receiver_id, receiver.nickname AS receiver_name,
                m.product_id, p.title AS product_title,
                m.content, m.read_flag, m.created_at
            FROM messages m
            LEFT JOIN users sender ON sender.id = m.sender_id
            LEFT JOIN users receiver ON receiver.id = m.receiver_id
            LEFT JOIN products p ON p.id = m.product_id
            WHERE m.sender_id = #{userId} OR m.receiver_id = #{userId}
            ORDER BY m.created_at DESC
            """)
    List<MessageView> findInbox(Long userId);

    @Select("""
            SELECT
                m.id, m.sender_id, sender.nickname AS sender_name,
                m.receiver_id, receiver.nickname AS receiver_name,
                m.product_id, p.title AS product_title,
                m.content, m.read_flag, m.created_at
            FROM messages m
            LEFT JOIN users sender ON sender.id = m.sender_id
            LEFT JOIN users receiver ON receiver.id = m.receiver_id
            LEFT JOIN products p ON p.id = m.product_id
            WHERE ((m.sender_id = #{userId} AND m.receiver_id = #{peerId})
                OR (m.sender_id = #{peerId} AND m.receiver_id = #{userId}))
              AND (#{productId} IS NULL OR m.product_id = #{productId})
            ORDER BY m.created_at ASC
            """)
    List<MessageView> findDialog(@Param("userId") Long userId,
                                 @Param("peerId") Long peerId,
                                 @Param("productId") Long productId);

    @Select("""
            SELECT
                m.id, m.sender_id, sender.nickname AS sender_name,
                m.receiver_id, receiver.nickname AS receiver_name,
                m.product_id, p.title AS product_title,
                m.content, m.read_flag, m.created_at
            FROM messages m
            LEFT JOIN users sender ON sender.id = m.sender_id
            LEFT JOIN users receiver ON receiver.id = m.receiver_id
            LEFT JOIN products p ON p.id = m.product_id
            ORDER BY m.created_at DESC
            LIMIT #{limit}
            """)
    List<MessageView> findLatest(@Param("limit") int limit);
}
