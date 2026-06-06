package com.sky.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * AI 搜索请求 DTO
 * 用户输入自然语言查询，由 AI 解析意图后调用工具函数搜索技能
 */
@Data
public class AiSearchDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户的自然语言查询内容
     * 例如："我想找一位心理咨询师，价格不要太贵"
     */
    @NotBlank(message = "查询内容不能为空")
    private String query;
}
