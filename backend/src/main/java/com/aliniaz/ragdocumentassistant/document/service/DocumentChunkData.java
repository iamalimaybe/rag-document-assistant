package com.aliniaz.ragdocumentassistant.document.service;

public record DocumentChunkData(
        Integer chunkIndex,
        Integer pageNumber,
        String content,
        String contentHash,
        Integer tokenEstimate
) {
}