package com.aliniaz.ragdocumentassistant.rag.api.response;

import com.aliniaz.ragdocumentassistant.rag.domain.RetrievedChunk;

import java.time.LocalDateTime;

public record RetrievedChunkSnapshotResponse(
        Long id,
        Long qaRunId,
        Long documentChunkId,
        Integer rank,
        Double similarityScore,
        String contentSnapshot,
        Integer pageNumberSnapshot,
        LocalDateTime createdAt
) {

    public static RetrievedChunkSnapshotResponse from(RetrievedChunk chunk) {
        return new RetrievedChunkSnapshotResponse(
                chunk.getId(),
                chunk.getQaRunId(),
                chunk.getDocumentChunkId(),
                chunk.getRank(),
                chunk.getSimilarityScore(),
                chunk.getContentSnapshot(),
                chunk.getPageNumberSnapshot(),
                chunk.getCreatedAt()
        );
    }
}