package com.aliniaz.ragdocumentassistant.document.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rag.chunking")
public record ChunkingProperties(
        int chunkSizeChars,
        int chunkOverlapChars,
        int approxCharsPerToken
) {

    public ChunkingProperties {
        if (chunkSizeChars < 1) {
            throw new IllegalArgumentException("chunkSizeChars must be greater than 0");
        }

        if (chunkOverlapChars < 0) {
            throw new IllegalArgumentException("chunkOverlapChars cannot be negative");
        }

        if (chunkOverlapChars >= chunkSizeChars) {
            throw new IllegalArgumentException("chunkOverlapChars must be smaller than chunkSizeChars");
        }

        if (approxCharsPerToken < 1) {
            throw new IllegalArgumentException("approxCharsPerToken must be greater than 0");
        }
    }
}