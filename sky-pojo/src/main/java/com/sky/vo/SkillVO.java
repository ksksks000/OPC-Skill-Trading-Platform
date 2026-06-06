package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 技能VO - 列表/详情展示
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillVO {

    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    private Long categoryId;

    private String categoryName;

    private Long sellerId;

    private String sellerName;

    private Integer status;

    private String tags;

    private String image;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
