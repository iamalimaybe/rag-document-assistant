package com.aliniaz.ragdocumentassistant.document.repository;

import com.aliniaz.ragdocumentassistant.document.service.SimilarDocumentChunk;

import java.util.List;

public interface DocumentChunkSearchStore {

    List<SimilarDocumentChunk> findSimilarChunks(Long documentId, String queryVector, int topK);

    long countEmbeddedChunks(Long documentId);
}