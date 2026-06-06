package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 聊天消息 Mapper
 *
 * 继承 MyBatis-Plus 的 BaseMapper，提供基础的 CRUD 操作。
 * 额外定义了历史消息查询和消息已读标记的自定义方法。
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 查询两个用户之间的聊天历史记录
     * 按时间升序排列，支持分页
     *
     * @param userId1 用户1的ID
     * @param userId2 用户2的ID
     * @param limit   最大返回条数
     * @return 聊天消息列表
     */
    @Select("SELECT * FROM chat_message " +
            "WHERE (sender_id = #{userId1} AND receiver_id = #{userId2}) " +
            "   OR (sender_id = #{userId2} AND receiver_id = #{userId1}) " +
            "ORDER BY create_time ASC LIMIT #{limit}")
    List<ChatMessage> getChatHistory(Long userId1, Long userId2, Integer limit);

    /**
     * 将发送给某用户的所有未读消息标记为已读
     *
     * @param receiverId 接收者ID（当前登录用户）
     * @param senderId   发送者ID（对方用户）
     */
    @Update("UPDATE chat_message SET is_read = 1 " +
            "WHERE receiver_id = #{receiverId} AND sender_id = #{senderId} AND is_read = 0")
    void markAsRead(Long receiverId, Long senderId);

    /**
     * 统计某用户的未读消息总数
     *
     * @param userId 用户ID
     * @return 未读消息数量
     */
    @Select("SELECT COUNT(*) FROM chat_message WHERE receiver_id = #{userId} AND is_read = 0")
    Integer countUnread(Long userId);
}
