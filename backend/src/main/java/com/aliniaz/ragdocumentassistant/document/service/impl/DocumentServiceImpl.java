package com.aliniaz.ragdocumentassistant.document.service.impl;

import com.aliniaz.ragdocumentassistant.document.api.response.DocumentResponse;
import com.aliniaz.ragdocumentassistant.document.domain.DocumentSourceType;
import com.aliniaz.ragdocumentassistant.document.domain.RagDocument;
import com.aliniaz.ragdocumentassistant.document.repository.RagDocumentRepository;
import com.aliniaz.ragdocumentassistant.document.service.DocumentService;
import com.aliniaz.ragdocumentassistant.document.service.DocumentTextExtractor;
import com.aliniaz.ragdocumentassistant.document.service.ExtractedDocumentText;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final RagDocumentRepository ragDocumentRepository;
    private final DocumentTextExtractor documentTextExtractor;

    @Override
    @Transactional
    public DocumentResponse upload(MultipartFile file) {
        validateFile(file);

        DocumentSourceType sourceType = resolveSourceType(file.getOriginalFilename());

        RagDocument document = new RagDocument(
                file.getOriginalFilename(),
                resolveContentType(file),
                sourceType
        );

        document = ragDocumentRepository.save(document);
        document.markProcessing();

        try {
            ExtractedDocumentText extracted = documentTextExtractor.extract(file, sourceType);

            if (extracted.text().isBlank()) {
                document.markFailed("No extractable text found in document");
            } else {
                document.markReady(extracted.text());
            }
        } catch (Exception e) {
            document.markFailed(rootMessage(e));
        }

        return DocumentResponse.from(document);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> findAll() {
        return ragDocumentRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(DocumentResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse findById(Long id) {
        return ragDocumentRepository.findById(id)
                .map(DocumentResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + id));
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Document file is required");
        }

        if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
            throw new IllegalArgumentException("Original filename is required");
        }
    }

    private DocumentSourceType resolveSourceType(String filename) {
        String lower = filename.toLowerCase();

        if (lower.endsWith(".pdf")) {
            return DocumentSourceType.PDF;
        }

        if (lower.endsWith(".txt")) {
            return DocumentSourceType.TXT;
        }

        if (lower.endsWith(".md")) {
            return DocumentSourceType.MD;
        }

        throw new IllegalArgumentException("Unsupported document type. Supported types: PDF, TXT, MD");
    }

    private String resolveContentType(MultipartFile file) {
        if (file.getContentType() == null || file.getContentType().isBlank()) {
            return "application/octet-stream";
        }

        return file.getContentType();
    }

    private String rootMessage(Exception e) {
        Throwable current = e;

        while (current.getCause() != null) {
            current = current.getCause();
        }

        return current.getMessage() == null ? "Document processing failed" : current.getMessage();
    }
}