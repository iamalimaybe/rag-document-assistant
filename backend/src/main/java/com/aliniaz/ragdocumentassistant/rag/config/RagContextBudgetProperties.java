package com.aliniaz.ragdocumentassistant.rag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rag.context-budget")
public record RagContextBudgetProperties(
        int maxPromptChunkTokens
) {

    public RagContextBudgetProperties {
        if (maxPromptChunkTokens < 1) {
            throw new IllegalArgumentException("maxPromptChunkTokens must be greater than 0");
        }
    }
}