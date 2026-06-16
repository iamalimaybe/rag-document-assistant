package com.aliniaz.ragdocumentassistant.document.service.impl;

import com.aliniaz.ragdocumentassistant.document.config.DocumentNormalizationProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LightDocumentTextNormalizerTest {

    private final LightDocumentTextNormalizer normalizer = new LightDocumentTextNormalizer(
            new DocumentNormalizationProperties(true, 1)
    );

    @Test
    void normalizeReturnsEmptyStringForNullInput() {
        String normalized = normalizer.normalize(null);

        assertEquals("", normalized);
    }

    @Test
    void normalizeRemovesBomAndTrimsOuterWhitespace() {
        String input = "\uFEFF   Document content   ";

        String normalized = normalizer.normalize(input);

        assertEquals("Document content", normalized);
    }

    @Test
    void normalizeStandardizesWindowsAndClassicMacLineEndings() {
        String input = "Heading\r\nFirst line\rSecond line\nThird line";

        String normalized = normalizer.normalize(input);

        assertEquals("Heading\nFirst line\nSecond line\nThird line", normalized);
    }

    @Test
    void normalizeCollapsesRepeatedSpacesAndTabsWithoutRemovingLineBreaks() {
        String input = "Heading\nThis    line\t\tcontains     spaces.\nNext\tline.";

        String normalized = normalizer.normalize(input);

        assertEquals("Heading\nThis line contains spaces.\nNext line.", normalized);
    }

    @Test
    void normalizeCanPreserveRepeatedSpacesAndTabsWhenConfigured() {
        LightDocumentTextNormalizer configuredNormalizer = new LightDocumentTextNormalizer(
                new DocumentNormalizationProperties(false, 1)
        );

        String input = "Heading\nThis    line\t\tkeeps     spacing.";

        String normalized = configuredNormalizer.normalize(input);

        assertEquals("Heading\nThis    line\t\tkeeps     spacing.", normalized);
    }

    @Test
    void normalizeReducesExcessiveBlankLinesToConfiguredLimit() {
        String input = "Section 1\n\n\n\nSection 2\n\n\nSection 3";

        String normalized = normalizer.normalize(input);

        assertEquals("Section 1\n\nSection 2\n\nSection 3", normalized);
    }

    @Test
    void normalizeCanAllowTwoConsecutiveBlankLinesWhenConfigured() {
        LightDocumentTextNormalizer configuredNormalizer = new LightDocumentTextNormalizer(
                new DocumentNormalizationProperties(true, 2)
        );

        String input = "Section 1\n\n\n\nSection 2";

        String normalized = configuredNormalizer.normalize(input);

        assertEquals("Section 1\n\n\nSection 2", normalized);
    }

    @Test
    void normalizePreservesHeadingsBulletsAndTableLikeLayout() {
        String input = """
                # Refund Policy

                - Refunds require approval
                - Processing time is 7 days

                Plan | Price | Support
                Basic | 10 | Email
                Pro | 20 | Email and chat
                """;

        String normalized = normalizer.normalize(input);

        String expected = """
                # Refund Policy

                - Refunds require approval
                - Processing time is 7 days

                Plan | Price | Support
                Basic | 10 | Email
                Pro | 20 | Email and chat
                """.trim();

        assertEquals(expected, normalized);
    }

    @Test
    void normalizationPropertiesRejectInvalidBlankLineLimit() {
        IllegalArgumentException exception = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new DocumentNormalizationProperties(true, 0)
        );

        assertEquals("maxConsecutiveBlankLines must be at least 1", exception.getMessage());
    }
}