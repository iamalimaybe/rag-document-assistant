package com.aliniaz.ragdocumentassistant.document.service.impl;

import com.aliniaz.ragdocumentassistant.document.config.DocumentNormalizationProperties;
import com.aliniaz.ragdocumentassistant.document.service.DocumentTextNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LightDocumentTextNormalizer implements DocumentTextNormalizer {

    private final DocumentNormalizationProperties documentNormalizationProperties;

    @Override
    public String normalize(String text) {
        if (text == null) {
            return "";
        }

        String normalized = text
                .replace("\uFEFF", "")
                .replace("\r\n", "\n")
                .replace('\r', '\n');

        if (documentNormalizationProperties.collapseSpacesAndTabs()) {
            normalized = normalized.replaceAll("[ \\t]+", " ");
        }

        normalized = reduceBlankLines(
                normalized,
                documentNormalizationProperties.maxConsecutiveBlankLines()
        );

        return normalized.trim();
    }

    private String reduceBlankLines(String text, int maxConsecutiveBlankLines) {
        int maxConsecutiveNewlines = maxConsecutiveBlankLines + 1;
        return text.replaceAll("\\n{" + (maxConsecutiveNewlines + 1) + ",}", "\n".repeat(maxConsecutiveNewlines));
    }
}