package com.aliniaz.ragdocumentassistant.llm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rag.llm")
public record LlmProperties(
        String model,
        Double temperature,
        Integer numPredict,
        Integer contextWindow
) {
}