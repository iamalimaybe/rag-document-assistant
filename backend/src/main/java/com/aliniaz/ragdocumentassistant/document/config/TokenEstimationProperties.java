package com.aliniaz.ragdocumentassistant.document.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rag.token-estimation")
public record TokenEstimationProperties(
        int approxCharsPerToken
) {

    public TokenEstimationProperties {
        if (approxCharsPerToken < 1) {
            throw new IllegalArgumentException("approxCharsPerToken must be greater than 0");
        }
    }
}