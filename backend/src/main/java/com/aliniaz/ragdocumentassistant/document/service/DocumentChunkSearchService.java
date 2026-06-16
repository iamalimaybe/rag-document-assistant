package com.aliniaz.ragdocumentassistant.document.service;

import java.util.List;

public interface DocumentChunkSearchService {

    List<SimilarDocumentChunk> findSimilarChunks(Long documentId, String queryVector, int topK);
}