package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * AI 搜索结果 VO
 * 包含 AI 推荐语和匹配的技能列表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSearchResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * AI 生成的推荐语
     * 基于用户意图和搜索结果，由 AI 撰写的暖心推荐文案
     */
    private String recommendationText;

    /**
     * 匹配的技能ID列表
     * AI 从工具函数返回结果中提取的技能ID
     */
    private List<Long> skillIds;
}
