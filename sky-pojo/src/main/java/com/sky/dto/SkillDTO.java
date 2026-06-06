package com.sky.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 技能DTO - 新增/修改
 */
@Data
public class SkillDTO {

    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    private Long categoryId;

    private Long sellerId;

    private Integer status;

    private String tags;

    private String image;
}
