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
 * 用户（买家/卖家共用）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    // 用户名
    private String username;

    // 密码
    private String password;

    // 姓名
    private String name;

    // 手机号
    private String phone;

    // 性别 0 女 1 男
    private String sex;

    // 身份证号
    private String idNumber;

    // 头像
    private String avatar;

    // 用户角色：0买家 1卖家 2管理员
    private Integer role;

    // 账号状态：1启用 0禁用
    private Integer status;

    // 注册时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;
}
