package com.sky.controller.user;

import com.sky.ai.AiSearchService;
import com.sky.dto.AiSearchDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AI 智搜控制器
 *
 * 提供 AI 驱动的技能搜索接口，支持 SSE（Server-Sent Events）流式输出。
 * 前端可以通过此接口实现"打字机效果"的 AI 推荐体验。
 *
 * 接口说明：
 * - POST /api/ai/search-stream ：流式搜索，通过 SSE 逐字推送 AI 响应（推荐）
 * - POST /api/ai/search-flux   ：流式搜索，通过 Flux<String> 直接返回（简洁）
 * - POST /api/ai/search        ：同步搜索，一次性返回完整结果
 *
 * SSE 数据格式：
 * - 每条消息以 "data: " 开头
 * - 消息内容为 AI 生成的一个文本片段
 * - 流结束发送 "data: [DONE]"
 */
@RestController
@RequestMapping("/api/ai")
@Slf4j
public class AiSearchController {

    @Autowired
    private AiSearchService aiSearchService;

    /**
     * 线程池，用于异步执行 AI 搜索任务
     * SseEmitter 方式需要异步执行，避免阻塞 Servlet 线程
     */
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * AI 智搜 - 流式输出（SSE 方式）
     *
     * 核心接口：前端发送自然语言查询，后端通过 SSE 逐字推送 AI 的响应。
     * AI 会自动调用 searchSkillsByIntent 工具函数搜索技能，
     * 然后生成包含推荐语和技能ID列表的 JSON 结果。
     *
     * 使用场景：前端使用 EventSource 或 fetch + ReadableStream 接收流式数据
     *
     * @param aiSearchDTO 包含 query 字段的请求体
     * @return SseEmitter 用于流式推送的发射器
     */
    @PostMapping(value = "/search-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter searchStream(@RequestBody AiSearchDTO aiSearchDTO) {
        log.info("AI 智搜流式请求（SSE），query：{}", aiSearchDTO.getQuery());

        // 创建 SseEmitter，设置超时时间为 120 秒（AI 响应可能较慢）
        SseEmitter emitter = new SseEmitter(120_000L);

        // 异步执行 AI 搜索，避免阻塞当前 Servlet 线程
        executor.execute(() -> {
            try {
                // 订阅 AI 流式响应
                aiSearchService.searchStream(aiSearchDTO.getQuery())
                        .subscribe(
                                // 每收到一个文本片段，通过 SSE 推送给前端
                                chunk -> {
                                    try {
                                        emitter.send(SseEmitter.event()
                                                .data(chunk)
                                                .id(String.valueOf(System.currentTimeMillis())));
                                    } catch (IOException e) {
                                        log.error("SSE 发送数据失败", e);
                                        emitter.completeWithError(e);
                                    }
                                },
                                // 发生错误时
                                error -> {
                                    log.error("AI 搜索流式输出异常", error);
                                    try {
                                        emitter.send(SseEmitter.event()
                                                .data("{\"error\":\"" + error.getMessage() + "\"}"));
                                    } catch (IOException ex) {
                                        // 忽略二次发送失败
                                    }
                                    emitter.completeWithError(error);
                                },
                                // 完成时发送 [DONE] 标记
                                () -> {
                                    try {
                                        emitter.send(SseEmitter.event().data("[DONE]"));
                                        emitter.complete();
                                    } catch (IOException e) {
                                        log.error("SSE 发送完成标记失败", e);
                                        emitter.completeWithError(e);
                                    }
                                }
                        );
            } catch (Exception e) {
                log.error("AI 搜索执行异常", e);
                emitter.completeWithError(e);
            }
        });

        // 设置超时和完成回调
        emitter.onTimeout(() -> {
            log.warn("SSE 连接超时");
            emitter.complete();
        });
        emitter.onCompletion(() -> log.info("SSE 连接完成"));

        return emitter;
    }

    /**
     * AI 智搜 - 流式输出（Flux 方式，更简洁）
     *
     * 直接返回 Flux<String>，Spring WebFlux 会自动将其转为 SSE 流。
     * 比手动管理 SseEmitter 更简洁，推荐前端使用 fetch API 接收。
     *
     * @param aiSearchDTO 包含 query 字段的请求体
     * @return Flux<String> 流式文本片段
     */
    @PostMapping(value = "/search-flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> searchFlux(@RequestBody AiSearchDTO aiSearchDTO) {
        log.info("AI 智搜流式请求（Flux），query：{}", aiSearchDTO.getQuery());
        return aiSearchService.searchStream(aiSearchDTO.getQuery());
    }

    /**
     * AI 智搜 - 同步输出
     *
     * 一次性返回 AI 的完整响应，适用于不需要流式效果的场景。
     * 返回内容为 JSON 字符串，包含 recommendationText 和 skillIds。
     *
     * @param aiSearchDTO 包含 query 字段的请求体
     * @return AI 生成的完整 JSON 响应
     */
    @PostMapping("/search")
    public String search(@RequestBody AiSearchDTO aiSearchDTO) {
        log.info("AI 智搜同步请求，query：{}", aiSearchDTO.getQuery());
        return aiSearchService.searchSync(aiSearchDTO.getQuery());
    }
}
