package com.aliniaz.ragdocumentassistant.embedding.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rag.ollama")
public record OllamaProperties(
        String baseUrl
) {
}