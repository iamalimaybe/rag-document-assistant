package com.aliniaz.ragdocumentassistant.document.service.impl;

import com.aliniaz.ragdocumentassistant.document.config.TokenEstimationProperties;
import com.aliniaz.ragdocumentassistant.document.service.TokenEstimator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApproximateTokenEstimator implements TokenEstimator {

    private final TokenEstimationProperties tokenEstimationProperties;

    @Override
    public Integer estimate(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Token estimation content cannot be blank");
        }

        return Math.max(
                1,
                (int) Math.ceil((double) content.length() / tokenEstimationProperties.approxCharsPerToken())
        );
    }
}