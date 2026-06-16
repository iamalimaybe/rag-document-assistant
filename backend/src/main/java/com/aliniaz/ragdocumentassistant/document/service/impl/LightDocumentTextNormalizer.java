package com.aliniaz.ragdocumentassistant.document.service.impl;

import com.aliniaz.ragdocumentassistant.document.service.DocumentTextNormalizer;
import org.springframework.stereotype.Component;

@Component
public class LightDocumentTextNormalizer implements DocumentTextNormalizer {

    @Override
    public String normalize(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replace("\uFEFF", "")
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }
}