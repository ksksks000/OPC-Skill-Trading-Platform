package com.sky.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 技能/服务商品
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("skill_info")
public class Skill implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    // 技能名称
    private String name;

    // 技能描述（富文本）
    private String description;

    // 价格
    private BigDecimal price;

    // 分类ID
    private Long categoryId;

    // 卖家ID（关联user表）
    private Long sellerId;

    // 状态：1上架 0下架
    private Integer status;

    // 标签（JSON格式，如 ["心理咨询","二级咨询师"]）
    private String tags;

    // 封面图
    private String image;

    // 创建时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;

    // 创建人
    private Long createUser;

    // 更新人
    private Long updateUser;
}
