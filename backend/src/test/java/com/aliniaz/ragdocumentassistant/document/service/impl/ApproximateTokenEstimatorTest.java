package com.aliniaz.ragdocumentassistant.document.service.impl;

import com.aliniaz.ragdocumentassistant.document.config.TokenEstimationProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApproximateTokenEstimatorTest {

    private final ApproximateTokenEstimator estimator =
            new ApproximateTokenEstimator(new TokenEstimationProperties(4));

    @Test
    void estimateReturnsAtLeastOneToken() {
        Integer tokens = estimator.estimate("abc");

        assertEquals(1, tokens);
    }

    @Test
    void estimateRoundsUpUsingConfiguredApproxCharsPerToken() {
        Integer tokens = estimator.estimate("a".repeat(9));

        assertEquals(3, tokens);
    }

    @Test
    void estimateRejectsNullContent() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> estimator.estimate(null)
        );

        assertEquals("Token estimation content cannot be blank", exception.getMessage());
    }

    @Test
    void estimateRejectsBlankContent() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> estimator.estimate("   ")
        );

        assertEquals("Token estimation content cannot be blank", exception.getMessage());
    }

    @Test
    void tokenEstimationPropertiesRejectInvalidApproxCharsPerToken() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new TokenEstimationProperties(0)
        );

        assertEquals("approxCharsPerToken must be greater than 0", exception.getMessage());
    }
}