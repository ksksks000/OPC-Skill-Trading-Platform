package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.entity.ChatMessage;
import com.sky.result.Result;
import com.sky.service.ChatMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 聊天控制器
 *
 * 提供聊天消息的发送和历史记录查询接口。
 * WebSocket 连接由 ChatWebSocketServer 处理，本控制器处理 HTTP 请求。
 *
 * 接口说明：
 * - POST /user/chat/send       ：发送聊天消息（保存到数据库 + WebSocket 推送）
 * - GET  /user/chat/history     ：获取与某用户的聊天历史记录
 * - PUT  /user/chat/read        ：标记对方发来的消息为已读
 * - GET  /user/chat/unread-count：获取当前用户的未读消息总数
 */
@RestController
@RequestMapping("/user/chat")
@Slf4j
public class ChatController {

    @Autowired
    private ChatMessageService chatMessageService;

    /**
     * 发送聊天消息
     *
     * 请求体格式：
     * {
     *   "receiverId": 2,       // 接收者用户ID
     *   "content": "你好！",   // 消息内容
     *   "type": "text"         // 消息类型（text/image）
     * }
     *
     * 发送者ID从 JWT Token 中提取（BaseContext.getCurrentId()）
     *
     * @param params 包含 receiverId、content、type 的请求体
     * @return 保存后的消息对象
     */
    @PostMapping("/send")
    public Result<ChatMessage> send(@RequestBody Map<String, Object> params) {
        Long senderId = BaseContext.getCurrentId();
        Long receiverId = Long.valueOf(params.get("receiverId").toString());
        String content = (String) params.get("content");
        String type = (String) params.getOrDefault("type", "text");

        log.info("发送聊天消息：sender={}, receiver={}, type={}", senderId, receiverId, type);

        ChatMessage message = chatMessageService.sendMessage(senderId, receiverId, content, type);
        return Result.success(message);
    }

    /**
     * 获取与某用户的聊天历史记录
     *
     * 返回两个用户之间的所有聊天消息，按时间升序排列。
     * 同时将对方发来的未读消息标记为已读。
     *
     * @param targetId 对方用户ID
     * @return 聊天消息列表
     */
    @GetMapping("/history")
    public Result<List<ChatMessage>> history(@RequestParam Long targetId) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("获取聊天历史：currentUserId={}, targetId={}", currentUserId, targetId);

        // 获取历史记录
        List<ChatMessage> messages = chatMessageService.getChatHistory(currentUserId, targetId);

        // 将对方发来的未读消息标记为已读
        chatMessageService.markAsRead(currentUserId, targetId);

        return Result.success(messages);
    }

    /**
     * 标记对方发来的消息为已读
     *
     * @param params 包含 senderId 的请求体（对方用户ID）
     */
    @PutMapping("/read")
    public Result markAsRead(@RequestBody Map<String, Object> params) {
        Long currentUserId = BaseContext.getCurrentId();
        Long senderId = Long.valueOf(params.get("senderId").toString());
        log.info("标记已读：currentUserId={}, senderId={}", currentUserId, senderId);

        chatMessageService.markAsRead(currentUserId, senderId);
        return Result.success();
    }

    /**
     * 获取当前用户的未读消息总数
     *
     * @return 未读消息数量
     */
    @GetMapping("/unread-count")
    public Result<Integer> unreadCount() {
        Long currentUserId = BaseContext.getCurrentId();
        Integer count = chatMessageService.countUnread(currentUserId);
        return Result.success(count);
    }
}
