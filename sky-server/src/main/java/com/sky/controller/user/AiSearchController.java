package com.sky.controller.user;

import com.sky.ai.AiSearchService;
import com.sky.dto.AiSearchDTO;
import com.sky.result.Result;
import jakarta.validation.Valid;
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
 * 提供 AI 驱动的技能搜索接口，支持 SSE 流式输出和同步输出。
 */
@RestController
@RequestMapping("/user/ai")
@Slf4j
public class AiSearchController {

    @Autowired
    private AiSearchService aiSearchService;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * AI 智搜 - 流式输出（SSE 方式）
     *
     * 前端发送自然语言查询，后端通过 SSE 逐字推送 AI 的响应。
     * 自动过滤 MiniMax 模型的思考过程标签。
     */
    @PostMapping(value = "/search-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter searchStream(@RequestBody @Valid AiSearchDTO aiSearchDTO) {
        log.info("AI 智搜流式请求（SSE），query：{}", aiSearchDTO.getQuery());

        SseEmitter emitter = new SseEmitter(120_000L);

        executor.execute(() -> {
            try {
                Flux<String> flux = aiSearchService.searchStream(aiSearchDTO.getQuery());
                // 跟踪是否在思考标签内
                boolean[] inThink = {false};

                flux.toStream().forEach(chunk -> {
                    try {
                        if (chunk == null || chunk.isEmpty()) return;

                        // 检测思考标签的开始和结束
                        if (chunk.contains("<think>") || chunk.contains("\u003Cthink\u003E")) {
                            inThink[0] = true;
                        }
                        if (chunk.contains("</think>") || chunk.contains("\u003C/think\u003E")) {
                            inThink[0] = false;
                            return;
                        }
                        // 在思考标签内，跳过
                        if (inThink[0]) return;

                        emitter.send(SseEmitter.event()
                                .data(chunk)
                                .id(String.valueOf(System.currentTimeMillis())));
                    } catch (IOException e) {
                        log.error("SSE 发送数据失败", e);
                        emitter.completeWithError(e);
                    }
                });

                emitter.send(SseEmitter.event().data("[DONE]"));
                emitter.complete();
            } catch (Exception e) {
                log.error("AI 搜索流式输出异常", e);
                try {
                    emitter.send(SseEmitter.event().data("{\"error\":\"AI服务异常\"}"));
                } catch (IOException ex) {
                    // 忽略
                }
                emitter.completeWithError(e);
            }
        });

        emitter.onTimeout(() -> {
            log.warn("SSE 连接超时");
            emitter.complete();
        });
        emitter.onCompletion(() -> log.info("SSE 连接完成"));

        return emitter;
    }

    /**
     * AI 智搜 - 流式输出（Flux 方式）
     */
    @PostMapping(value = "/search-flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> searchFlux(@RequestBody @Valid AiSearchDTO aiSearchDTO) {
        log.info("AI 智搜流式请求（Flux），query：{}", aiSearchDTO.getQuery());
        return aiSearchService.searchStream(aiSearchDTO.getQuery());
    }

    /**
     * AI 智搜 - 同步输出
     *
     * 一次性返回 AI 的完整响应，已过滤思考过程标签。
     */
    @PostMapping("/search")
    public Result<String> search(@RequestBody @Valid AiSearchDTO aiSearchDTO) {
        log.info("AI 智搜同步请求，query：{}", aiSearchDTO.getQuery());
        try {
            String result = aiSearchService.searchSync(aiSearchDTO.getQuery());
            return Result.success(result);
        } catch (Exception e) {
            log.error("AI 搜索服务异常", e);
            return Result.error("AI 搜索服务暂时不可用，请稍后再试");
        }
    }
}
