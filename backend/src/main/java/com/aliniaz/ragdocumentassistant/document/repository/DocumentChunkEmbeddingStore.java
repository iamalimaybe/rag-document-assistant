package com.aliniaz.ragdocumentassistant.document.repository;

public interface DocumentChunkEmbeddingStore {

    void updateEmbedding(Long chunkId, String vector);
}