package com.qihang.campusmarket.service;

import com.qihang.campusmarket.dto.MessageView;
import com.qihang.campusmarket.entity.Message;
import com.qihang.campusmarket.mapper.MessageMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class MessageService {
    private final MessageMapper messageMapper;

    public MessageService(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    public List<MessageView> inbox(Long userId) {
        return messageMapper.findInbox(userId);
    }

    public List<MessageView> dialog(Long userId, Long peerId, Long productId) {
        return messageMapper.findDialog(userId, peerId, productId);
    }

    @Transactional
    public void send(Long senderId, Long receiverId, Long productId, String content) {
        if (!StringUtils.hasText(content)) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("不能给自己发送消息");
        }
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setProductId(productId);
        message.setContent(content.trim());
        message.setReadFlag(false);
        messageMapper.insert(message);
    }
}
