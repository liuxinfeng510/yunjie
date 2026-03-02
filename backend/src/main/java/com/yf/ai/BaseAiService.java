package com.yf.ai;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 基础 AI 服务
 * 封装阿里云 DashScope API 调用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BaseAiService {

    private final AiConfig aiConfig;

    /**
     * 文本对话
     */
    public String chat(String systemPrompt, String userMessage) {
        long startTime = System.currentTimeMillis();
        try {
            Generation gen = new Generation();
            Message systemMsg = Message.builder()
                    .role(Role.SYSTEM.getValue())
                    .content(systemPrompt)
                    .build();
            Message userMsg = Message.builder()
                    .role(Role.USER.getValue())
                    .content(userMessage)
                    .build();

            GenerationParam param = GenerationParam.builder()
                    .apiKey(aiConfig.getApiKey())
                    .model(aiConfig.getTextModel())
                    .messages(Arrays.asList(systemMsg, userMsg))
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .build();

            GenerationResult result = gen.call(param);
            String content = result.getOutput().getChoices().get(0).getMessage().getContent();
            log.info("AI Chat completed in {}ms, tokens: {}",
                    System.currentTimeMillis() - startTime,
                    result.getUsage().getTotalTokens());
            return content;
        } catch (Exception e) {
            log.error("AI Chat failed", e);
            throw new RuntimeException("AI 服务调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 视觉识别（图片+文本）
     */
    public String vision(String systemPrompt, String userPrompt, String imageUrl) {
        long startTime = System.currentTimeMillis();
        try {
            MultiModalConversation conv = new MultiModalConversation();

            Map<String, Object> imageContent = new HashMap<>();
            imageContent.put("image", imageUrl);

            Map<String, Object> textContent = new HashMap<>();
            textContent.put("text", userPrompt);

            MultiModalMessage userMessage = MultiModalMessage.builder()
                    .role(Role.USER.getValue())
                    .content(Arrays.asList(imageContent, textContent))
                    .build();

            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .apiKey(aiConfig.getApiKey())
                    .model(aiConfig.getVisionModel())
                    .messages(Collections.singletonList(userMessage))
                    .build();

            MultiModalConversationResult result = conv.call(param);
            String content = result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text").toString();
            log.info("AI Vision completed in {}ms", System.currentTimeMillis() - startTime);
            return content;
        } catch (Exception e) {
            log.error("AI Vision failed", e);
            throw new RuntimeException("AI 视觉服务调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 视觉识别（Base64图片）
     */
    public String visionBase64(String systemPrompt, String userPrompt, String base64Image) {
        // DashScope 支持 data URI 格式
        String dataUri = "data:image/jpeg;base64," + base64Image;
        return vision(systemPrompt, userPrompt, dataUri);
    }
}
