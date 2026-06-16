package com.aliniaz.ragdocumentassistant.embedding.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rag.embedding")
public record EmbeddingProperties(
        String model
) {
}