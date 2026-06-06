package com.sky.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.sky.entity.Skill;
import com.sky.service.SkillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.List;
import java.util.function.Function;

/**
 * AI 工具函数配置类
 *
 * 将技能搜索功能注册为 Spring Bean，供 Spring AI 的 Function Calling 机制自动调用。
 * 当用户输入自然语言查询时，AI 模型会解析出参数并自动调用这些函数。
 *
 * 注册方式：
 * - 使用 @Bean + @Description 注解将函数注册为 Spring Bean
 * - @Description 描述函数用途，帮助 AI 判断何时调用
 * - 请求参数使用 record 类型，配合 @JsonPropertyDescription 描述每个字段
 * - AI 会根据描述自动将自然语言映射为函数参数
 */
@Slf4j
@Configuration
public class AiFunctionConfig {

    private final SkillService skillService;

    public AiFunctionConfig(SkillService skillService) {
        this.skillService = skillService;
    }

    /**
     * 注册 AI 工具函数：根据意图搜索技能
     *
     * Bean 名称 "searchSkillsByIntent" 即为 AI 可调用的函数名。
     * AI 在处理用户查询时，如果判断需要搜索技能，会自动调用此函数。
     *
     * @return Function<Request, Response> 类型的函数 Bean
     */
    @Bean
    @Description("根据用户的意图搜索技能/服务。可以通过分类、标签、最低价格来筛选技能。当用户想找某种服务或技能时调用此函数。")
    public Function<SearchSkillsRequest, SearchSkillsResponse> searchSkillsByIntent() {
        return request -> {
            log.info("AI 调用工具函数 searchSkillsByIntent，参数：category={}, tag={}, minPrice={}",
                    request.category(), request.tag(), request.minPrice());

            // 调用 SkillService 的搜索方法查询数据库
            List<Skill> skills = skillService.searchSkills(
                    request.category(),
                    request.tag(),
                    request.minPrice()
            );

            log.info("搜索到 {} 条技能", skills.size());
            return new SearchSkillsResponse(skills);
        };
    }

    /**
     * 搜索请求参数
     *
     * AI 会根据用户的自然语言自动填充这些字段：
     * - 用户说"心理咨询" -> category="心理咨询"
     * - 用户说"不要太贵" -> minPrice=0
     * - 用户说"有Python相关的吗" -> tag="Python"
     *
     * @param category  技能分类
     * @param tag       技能标签关键词
     * @param minPrice  最低价格筛选
     */
    public record SearchSkillsRequest(
            @JsonProperty("category")
            @JsonPropertyDescription("技能分类，例如：心理咨询、编程教学、法律咨询、设计")
            String category,

            @JsonProperty("tag")
            @JsonPropertyDescription("技能标签关键词，例如：二级咨询师、Python、塔罗牌、简历优化")
            String tag,

            @JsonProperty("minPrice")
            @JsonPropertyDescription("最低价格筛选，单位：元。例如用户说'不要太贵'可设为0，'高端'可设为500")
            Double minPrice
    ) {}

    /**
     * 搜索响应结果
     *
     * @param skills 匹配的技能列表，AI 会从结果中提取 skillIds 用于最终响应
     */
    public record SearchSkillsResponse(List<Skill> skills) {}
}
