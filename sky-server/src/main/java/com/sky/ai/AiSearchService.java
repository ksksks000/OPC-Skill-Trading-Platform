package com.sky.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
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
 */
@Slf4j
@Service
public class AiSearchService {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = """
            你是 OPC 技能交易平台的智能导购助手。你的任务是帮助用户找到最合适的技能/服务。

            ## 工作流程
            1. 分析用户的自然语言输入，理解他们的需求
            2. 调用 searchSkillsByIntent 工具函数搜索匹配的技能
            3. 根据搜索结果，撰写一段暖心的推荐语
            4. 以严格的 JSON 格式返回结果

            ## 搜索技巧（重要！）
            - category 参数应填写用户需求的核心关键词，如"心理咨询"、"编程"、"简历"、"法律"、"设计"、"占卜"等
            - 如果用户的需求比较模糊，尝试用更宽泛的关键词搜索，如"咨询"、"教学"
            - 不要传入过于具体或生僻的 category 值，这可能导致搜索不到结果
            - tag 参数用于进一步筛选，如"Python"、"二级咨询师"等
            - 如果第一次搜索结果为空，请用更宽泛的关键词再搜索一次

            ## 推荐语撰写规范
            - 语气温暖亲切，像朋友在推荐
            - 突出每个技能的亮点和适合的人群
            - 如果搜索到多个技能，简要对比差异
            - 如果没有搜索到结果，给出替代建议
            - 推荐语长度控制在 100-200 字

            ## 返回格式（严格遵守）
            必须返回以下 JSON 格式，不要添加任何其他文字，不要输出思考过程：
            {
              "recommendationText": "你的推荐语文本",
              "skillIds": [1, 2, 3]
            }

            ## 注意事项
            - skillIds 必须从搜索结果中提取，不要编造
            - 如果搜索结果为空，skillIds 为空数组，推荐语中说明未找到并给出建议
            - 只返回 JSON，不要包含 markdown 代码块标记
            - 不要输出思考过程，直接返回 JSON 结果
            """;

    public AiSearchService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * 流式搜索 - 通过 SSE 推送 AI 响应
     */
    public Flux<String> searchStream(String query) {
        log.info("AI 智搜开始（流式），用户查询：{}", query);

        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(query)
                .functions("searchSkillsByIntent")
                .stream()
                .content();
    }

    /**
     * 同步搜索 - 一次性返回完整的搜索结果
     */
    public String searchSync(String query) {
        log.info("AI 智搜开始（同步），用户查询：{}", query);

        String result = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(query)
                .functions("searchSkillsByIntent")
                .call()
                .content();

        // 过滤掉模型的思考过程输出（<think>...</think> 标签）
        return filterThinkTags(result);
    }

    /**
     * 过滤模型输出中的思考标签
     * MiniMax 等模型可能会输出 <think>...</think> 标签包裹的思考过程，
     * 需要移除这些内容，只保留最终的 JSON 结果。
     */
    private String filterThinkTags(String content) {
        if (content == null) return null;
        // 移除 <think>...</think> 标签及其内容
        return content.replaceAll("(?s)<think>.*?</think>", "").trim();
    }
}
