package com.aliniaz.ragdocumentassistant.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rag.ollama")
public record OllamaProperties(
        String baseUrl
) {
}