package com.aliniaz.ragdocumentassistant.document.service.impl;

import com.aliniaz.ragdocumentassistant.document.domain.DocumentSourceType;
import com.aliniaz.ragdocumentassistant.document.service.DocumentTextExtractor;
import com.aliniaz.ragdocumentassistant.document.service.ExtractedDocumentText;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class DocumentTextExtractorImpl implements DocumentTextExtractor {

    @Override
    public ExtractedDocumentText extract(MultipartFile file, DocumentSourceType sourceType) {
        try {
            return switch (sourceType) {
                case PDF -> extractPdf(file);
                case TXT, MD -> extractPlainText(file);
            };
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to extract text from document", e);
        }
    }

    private ExtractedDocumentText extractPdf(MultipartFile file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = normalizeText(stripper.getText(document));
            return new ExtractedDocumentText(text);
        }
    }

    private ExtractedDocumentText extractPlainText(MultipartFile file) throws IOException {
        String text = new String(file.getBytes(), StandardCharsets.UTF_8);
        return new ExtractedDocumentText(normalizeText(text));
    }

    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .trim();
    }
}