package com.yf.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AI 配置属性
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai.dashscope")
public class AiConfig {

    /** API Key */
    private String apiKey = "sk-demo-key";

    /** 文本模型 */
    private String textModel = "qwen-max";

    /** 视觉模型 */
    private String visionModel = "qwen-vl-max";

    /** 超时时间(ms) */
    private int timeout = 60000;
}
