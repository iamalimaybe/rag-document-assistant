package com.aliniaz.ragdocumentassistant.document.service;

public interface DocumentEmbeddingService {

    EmbeddedDocumentResponse embedDocumentChunks(Long documentId);
}