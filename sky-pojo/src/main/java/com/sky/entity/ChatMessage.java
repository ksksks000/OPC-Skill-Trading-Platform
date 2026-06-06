package com.sky.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天消息实体
 *
 * 对应数据库表 chat_message，记录买卖双方的聊天记录。
 * 支持文本和图片两种消息类型。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("chat_message")
public class ChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发送者用户ID */
    private Long senderId;

    /** 接收者用户ID */
    private Long receiverId;

    /** 消息内容（文本内容或图片URL） */
    private String content;

    /** 消息类型：text文本 image图片 */
    private String type;

    /** 发送时间 */
    private LocalDateTime createTime;

    /** 是否已读：0未读 1已读 */
    private Integer isRead;
}
