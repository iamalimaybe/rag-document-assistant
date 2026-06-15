package com.aliniaz.ragdocumentassistant.document.api.response;

import com.aliniaz.ragdocumentassistant.document.domain.DocumentSourceType;
import com.aliniaz.ragdocumentassistant.document.domain.DocumentStatus;
import com.aliniaz.ragdocumentassistant.document.domain.RagDocument;

import java.time.LocalDateTime;

public record DocumentResponse(
        Long id,
        String originalFilename,
        String contentType,
        DocumentStatus status,
        DocumentSourceType sourceType,
        Integer extractedTextLength,
        String failureReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static DocumentResponse from(RagDocument document) {
        return new DocumentResponse(
                document.getId(),
                document.getOriginalFilename(),
                document.getContentType(),
                document.getStatus(),
                document.getSourceType(),
                document.getExtractedTextLength(),
                document.getFailureReason(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}