package com.aliniaz.ragdocumentassistant.document.api.response;

import com.aliniaz.ragdocumentassistant.document.domain.DocumentChunk;

import java.time.LocalDateTime;

public record DocumentChunkResponse(
        Long id,
        Long documentId,
        Integer chunkIndex,
        Integer pageNumber,
        String content,
        String contentHash,
        Integer tokenEstimate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static DocumentChunkResponse from(DocumentChunk chunk) {
        return new DocumentChunkResponse(
                chunk.getId(),
                chunk.getDocumentId(),
                chunk.getChunkIndex(),
                chunk.getPageNumber(),
                chunk.getContent(),
                chunk.getContentHash(),
                chunk.getTokenEstimate(),
                chunk.getCreatedAt(),
                chunk.getUpdatedAt()
        );
    }
}