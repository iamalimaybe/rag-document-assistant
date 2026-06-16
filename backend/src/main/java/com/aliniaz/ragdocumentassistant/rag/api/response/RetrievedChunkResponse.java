package com.aliniaz.ragdocumentassistant.rag.api.response;

import com.aliniaz.ragdocumentassistant.document.service.SimilarDocumentChunk;

public record RetrievedChunkResponse(
        Long chunkId,
        Long documentId,
        Integer chunkIndex,
        Integer pageNumber,
        Double similarityScore,
        Integer tokenEstimate,
        String content
) {

    public static RetrievedChunkResponse from(SimilarDocumentChunk chunk) {
        return new RetrievedChunkResponse(
                chunk.chunkId(),
                chunk.documentId(),
                chunk.chunkIndex(),
                chunk.pageNumber(),
                chunk.similarityScore(),
                chunk.tokenEstimate(),
                chunk.content()
        );
    }
}