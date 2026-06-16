package com.aliniaz.ragdocumentassistant.document.service;

public record SimilarDocumentChunk(
        Long chunkId,
        Long documentId,
        Integer chunkIndex,
        Integer pageNumber,
        String content,
        String contentHash,
        Integer tokenEstimate,
        Double similarityScore
) {
}