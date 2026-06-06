package com.sky.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天专用 WebSocket 服务端
 *
 * 基于 JSR-356 (Jakarta WebSocket) 实现的点对点聊天 WebSocket 端点。
 * 每个用户连接时使用自己的用户ID作为 sid（会话标识），
 * 服务端维护 userId -> Session 的映射，支持向指定用户推送消息。
 *
 * 连接地址：ws://host:port/ws/chat/{userId}
 *
 * 消息推送机制：
 * - sendToUser: 向指定用户推送消息（点对点）
 * - sendToAllClient: 向所有在线用户广播消息（保留兼容）
 *
 * 线程安全：
 * - 使用 ConcurrentHashMap 替代 HashMap，保证多线程环境下的安全性
 */
@Slf4j
@Component
@ServerEndpoint("/ws/chat/{userId}")
public class ChatWebSocketServer {

    /**
     * 在线用户会话映射
     * Key: 用户ID（字符串形式）
     * Value: WebSocket Session 对象
     *
     * 使用 ConcurrentHashMap 保证线程安全，
     * 多个用户同时连接/断开时不会出现并发问题
     */
    private static final ConcurrentHashMap<String, Session> SESSION_MAP = new ConcurrentHashMap<>();

    /**
     * 连接建立成功回调
     *
     * 当用户打开聊天页面，前端建立 WebSocket 连接时触发。
     * 将用户的 Session 保存到映射表中，以便后续推送消息。
     *
     * @param session WebSocket 会话
     * @param userId  用户ID（从路径参数获取）
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        log.info("聊天 WebSocket 连接建立：userId={}", userId);
        SESSION_MAP.put(userId, session);
    }

    /**
     * 收到客户端消息回调
     *
     * 当前端通过 WebSocket 发送消息时触发。
     * 当前实现中，消息的发送逻辑由 HTTP 接口处理（ChatController），
     * WebSocket 仅用于服务端向客户端推送，因此此方法主要用于心跳检测。
     *
     * @param message 客户端发送的消息
     * @param userId  发送者用户ID
     */
    @OnMessage
    public void onMessage(String message, @PathParam("userId") String userId) {
        log.debug("收到用户 {} 的 WebSocket 消息：{}", userId, message);
    }

    /**
     * 连接关闭回调
     *
     * 用户关闭聊天页面或网络断开时触发。
     * 从映射表中移除该用户的 Session。
     *
     * @param userId 断开连接的用户ID
     */
    @OnClose
    public void onClose(@PathParam("userId") String userId) {
        log.info("聊天 WebSocket 连接关闭：userId={}", userId);
        SESSION_MAP.remove(userId);
    }

    /**
     * 向指定用户推送消息（点对点）
     *
     * 这是聊天功能的核心方法。发送消息时：
     * 1. 根据 userId 查找该用户的 WebSocket Session
     * 2. 如果用户在线，通过 Session 推送 JSON 格式的消息
     * 3. 如果用户不在线，消息仍保存在数据库中，下次拉取历史记录时可见
     *
     * @param userId  目标用户ID
     * @param message 要推送的消息（JSON 格式字符串）
     */
    public void sendToUser(String userId, String message) {
        Session session = SESSION_MAP.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
                log.debug("消息已推送给用户：{}", userId);
            } catch (IOException e) {
                log.error("推送消息给用户 {} 失败", userId, e);
            }
        } else {
            log.debug("用户 {} 不在线，消息将通过历史记录获取", userId);
        }
    }

    /**
     * 向所有在线用户广播消息
     * 保留兼容原 WebSocketServer 的群发功能
     *
     * @param message 要广播的消息
     */
    public void sendToAllClient(String message) {
        Collection<Session> sessions = SESSION_MAP.values();
        for (Session session : sessions) {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    log.error("广播消息失败", e);
                }
            }
        }
    }

    /**
     * 检查指定用户是否在线
     *
     * @param userId 用户ID
     * @return true=在线，false=离线
     */
    public boolean isOnline(String userId) {
        Session session = SESSION_MAP.get(userId);
        return session != null && session.isOpen();
    }

    /**
     * 获取当前在线用户数量
     *
     * @return 在线用户数
     */
    public int getOnlineCount() {
        return SESSION_MAP.size();
    }
}
