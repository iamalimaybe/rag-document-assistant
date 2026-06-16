package com.aliniaz.ragdocumentassistant.document.service.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LightDocumentTextNormalizerTest {

    private final LightDocumentTextNormalizer normalizer = new LightDocumentTextNormalizer();

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
    void normalizeReducesExcessiveBlankLinesToSingleBlankLine() {
        String input = "Section 1\n\n\n\nSection 2\n\n\nSection 3";

        String normalized = normalizer.normalize(input);

        assertEquals("Section 1\n\nSection 2\n\nSection 3", normalized);
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
}