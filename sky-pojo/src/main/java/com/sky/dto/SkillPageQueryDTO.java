package com.sky.dto;

import lombok.Data;

/**
 * 技能分页查询DTO
 */
@Data
public class SkillPageQueryDTO {

    private int page;

    private int pageSize;

    private String name;

    private Long categoryId;

    private Integer status;

    private Long sellerId;
}
