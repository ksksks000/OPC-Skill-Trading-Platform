package com.sky.service;

import com.sky.entity.ChatMessage;

import java.util.List;

/**
 * 聊天消息服务接口
 *
 * 提供聊天消息的发送、查询、已读标记等功能。
 * 消息发送后同时通过 WebSocket 实时推送给接收方。
 */
public interface ChatMessageService {

    /**
     * 发送聊天消息
     * 保存消息到数据库，并通过 WebSocket 推送给接收方
     *
     * @param senderId   发送者ID
     * @param receiverId 接收者ID
     * @param content    消息内容
     * @param type       消息类型（text/image）
     * @return 保存后的消息对象（含ID和时间戳）
     */
    ChatMessage sendMessage(Long senderId, Long receiverId, String content, String type);

    /**
     * 获取两个用户之间的聊天历史记录
     *
     * @param userId1 用户1的ID
     * @param userId2 用户2的ID
     * @return 按时间升序排列的消息列表
     */
    List<ChatMessage> getChatHistory(Long userId1, Long userId2);

    /**
     * 将对方发来的消息标记为已读
     *
     * @param receiverId 当前用户ID（接收方）
     * @param senderId   对方用户ID（发送方）
     */
    void markAsRead(Long receiverId, Long senderId);

    /**
     * 获取某用户的未读消息总数
     *
     * @param userId 用户ID
     * @return 未读消息数量
     */
    Integer countUnread(Long userId);
}
