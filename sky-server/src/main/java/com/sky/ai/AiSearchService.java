package com.sky.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * AI 智搜服务
 *
 * 核心功能：接收用户的自然语言查询，通过 Spring AI 的 Function Calling 机制
 * 自动调用 searchSkillsByIntent 工具函数搜索技能，并生成暖心的推荐语。
 *
 * 工作流程：
 * 1. 用户输入自然语言 query（如"我想找一位心理咨询师"）
 * 2. AI 解析用户意图，提取 category/tag/minPrice 参数
 * 3. AI 自动调用 searchSkillsByIntent 工具函数查询数据库
 * 4. AI 根据搜索结果生成推荐语，以 JSON 格式返回
 * 5. 结果通过 SSE 流式推送给前端
 *
 * 技术要点：
 * - 使用 ChatClient 与 AI 模型交互
 * - 通过 .functions("searchSkillsByIntent") 注册工具函数
 * - System Prompt 定义 AI 的角色、行为规范和输出格式
 * - 支持流式（Flux<String>）和同步两种调用模式
 */
@Slf4j
@Service
public class AiSearchService {

    /** ChatClient - Spring AI 的核心对话客户端 */
    private final ChatClient chatClient;

    /** AI 模型名称，从配置文件读取（支持 OpenAI/DeepSeek/智谱等兼容接口） */
    @Value("${sky.ai.model:deepseek-chat}")
    private String modelName;

    /**
     * System Prompt - 定义 AI 的角色和行为规范
     *
     * 这是最关键的部分，决定了 AI 如何理解用户意图和调用工具。
     * 包含以下要素：
     * - 角色定义：OPC 技能交易平台的智能导购助手
     * - 工作流程：分析意图 -> 调用工具 -> 生成推荐
     * - 推荐语撰写规范：温暖亲切、突出亮点、对比差异
     * - 返回格式：严格的 JSON 结构（recommendationText + skillIds）
     */
    private static final String SYSTEM_PROMPT = """
            你是 OPC 技能交易平台的智能导购助手。你的任务是帮助用户找到最合适的技能/服务。

            ## 工作流程
            1. 分析用户的自然语言输入，理解他们的需求
            2. 调用 searchSkillsByIntent 工具函数搜索匹配的技能
            3. 根据搜索结果，撰写一段暖心的推荐语
            4. 以严格的 JSON 格式返回结果

            ## 推荐语撰写规范
            - 语气温暖亲切，像朋友在推荐
            - 突出每个技能的亮点和适合的人群
            - 如果搜索到多个技能，简要对比差异
            - 如果没有搜索到结果，给出替代建议
            - 推荐语长度控制在 100-200 字

            ## 返回格式（严格遵守）
            必须返回以下 JSON 格式，不要添加任何其他文字：
            {
              "recommendationText": "你的推荐语文本",
              "skillIds": [1, 2, 3]
            }

            ## 注意事项
            - skillIds 必须从搜索结果中提取，不要编造
            - 如果搜索结果为空，skillIds 为空数组，推荐语中说明未找到并给出建议
            - 只返回 JSON，不要包含 markdown 代码块标记
            """;

    /**
     * 构造函数 - 注入 ChatClient.Builder
     *
     * Spring AI 会自动配置 ChatClient.Builder Bean，
     * 其中包含 application.yml 中配置的 API Key、Base URL 等信息。
     *
     * @param chatClientBuilder Spring AI 自动注入的 ChatClient 构建器
     */
    public AiSearchService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * 流式搜索 - 通过 SSE 推送 AI 的思考过程和最终结果
     *
     * 使用场景：前端需要"打字机效果"，逐字显示 AI 的响应。
     * AI 会先调用工具函数搜索技能，然后流式输出推荐语和技能列表。
     *
     * @param query 用户的自然语言查询
     * @return Flux<String> 流式输出的文本片段，前端逐片段拼接即可
     */
    public Flux<String> searchStream(String query) {
        log.info("AI 智搜开始（流式），用户查询：{}", query);

        // 使用 ChatClient 的流式 API
        // .functions("searchSkillsByIntent") 注册工具函数，AI 会自动判断是否需要调用
        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(query)
                .functions("searchSkillsByIntent")
                .stream()
                .content();
    }

    /**
     * 同步搜索 - 一次性返回完整的搜索结果
     *
     * 使用场景：不需要流式输出的场景，如后台任务、测试等。
     * 返回内容为 JSON 字符串，包含 recommendationText 和 skillIds。
     *
     * @param query 用户的自然语言查询
     * @return AI 生成的完整 JSON 响应
     */
    public String searchSync(String query) {
        log.info("AI 智搜开始（同步），用户查询：{}", query);

        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(query)
                .functions("searchSkillsByIntent")
                .call()
                .content();
    }
}
