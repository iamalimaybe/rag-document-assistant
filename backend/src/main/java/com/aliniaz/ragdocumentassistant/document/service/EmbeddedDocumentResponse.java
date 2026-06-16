package com.aliniaz.ragdocumentassistant.document.service;

public record EmbeddedDocumentResponse(
        Long documentId,
        int embeddedChunkCount
) {
}