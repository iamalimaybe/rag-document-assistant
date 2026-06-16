package com.aliniaz.ragdocumentassistant.document.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rag.normalization")
public record DocumentNormalizationProperties(
        boolean collapseSpacesAndTabs,
        int maxConsecutiveBlankLines
) {

    public DocumentNormalizationProperties {
        if (maxConsecutiveBlankLines < 1) {
            throw new IllegalArgumentException("maxConsecutiveBlankLines must be at least 1");
        }
    }
}