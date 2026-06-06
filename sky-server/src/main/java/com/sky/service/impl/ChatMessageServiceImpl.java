package com.sky.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.entity.ChatMessage;
import com.sky.mapper.ChatMessageMapper;
import com.sky.service.ChatMessageService;
import com.sky.websocket.ChatWebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天消息服务实现
 *
 * 核心逻辑：
 * 1. 发送消息时，先保存到数据库，再通过 WebSocket 实时推送给接收方
 * 2. 推送的消息格式为 JSON，包含发送者ID、接收者ID、消息内容等
 * 3. 如果接收方不在线，消息仍会保存在数据库中，下次拉取历史记录时可见
 */
@Slf4j
@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private ChatWebSocketServer chatWebSocketServer;

    /** JSON 序列化工具 */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 发送聊天消息
     *
     * 流程：
     * 1. 构建消息对象，设置发送时间和未读状态
     * 2. 保存到数据库
     * 3. 通过 WebSocket 推送给接收方（如果在线）
     *
     * @param senderId   发送者ID
     * @param receiverId 接收者ID
     * @param content    消息内容
     * @param type       消息类型
     * @return 保存后的消息对象
     */
    @Override
    public ChatMessage sendMessage(Long senderId, Long receiverId, String content, String type) {
        // 构建消息对象
        ChatMessage message = ChatMessage.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .content(content)
                .type(type != null ? type : "text")
                .createTime(LocalDateTime.now())
                .isRead(0)
                .build();

        // 保存到数据库
        chatMessageMapper.insert(message);
        log.info("聊天消息已保存：sender={}, receiver={}, type={}", senderId, receiverId, type);

        // 通过 WebSocket 实时推送给接收方
        try {
            Map<String, Object> pushMsg = new HashMap<>();
            pushMsg.put("id", message.getId());
            pushMsg.put("senderId", senderId);
            pushMsg.put("receiverId", receiverId);
            pushMsg.put("content", content);
            pushMsg.put("type", message.getType());
            pushMsg.put("createTime", message.getCreateTime().toString());

            String json = objectMapper.writeValueAsString(pushMsg);
            // 推送给接收方（使用用户ID作为 sid）
            chatWebSocketServer.sendToUser(String.valueOf(receiverId), json);
            log.info("WebSocket 推送消息给用户：{}", receiverId);
        } catch (Exception e) {
            log.error("WebSocket 推送消息失败", e);
            // 推送失败不影响消息保存，接收方可以通过拉取历史记录获取
        }

        return message;
    }

    /**
     * 获取聊天历史记录
     * 默认返回最近 100 条消息
     */
    @Override
    public List<ChatMessage> getChatHistory(Long userId1, Long userId2) {
        return chatMessageMapper.getChatHistory(userId1, userId2, 100);
    }

    /**
     * 标记消息为已读
     */
    @Override
    public void markAsRead(Long receiverId, Long senderId) {
        chatMessageMapper.markAsRead(receiverId, senderId);
    }

    /**
     * 统计未读消息数
     */
    @Override
    public Integer countUnread(Long userId) {
        return chatMessageMapper.countUnread(userId);
    }
}
