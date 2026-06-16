package com.aliniaz.ragdocumentassistant.document.service.impl;

import com.aliniaz.ragdocumentassistant.document.domain.DocumentStatus;
import com.aliniaz.ragdocumentassistant.document.domain.RagDocument;
import com.aliniaz.ragdocumentassistant.document.repository.DocumentChunkSearchStore;
import com.aliniaz.ragdocumentassistant.document.repository.RagDocumentRepository;
import com.aliniaz.ragdocumentassistant.document.service.DocumentChunkSearchService;
import com.aliniaz.ragdocumentassistant.document.service.SimilarDocumentChunk;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentChunkSearchServiceImpl implements DocumentChunkSearchService {

    private final RagDocumentRepository ragDocumentRepository;
    private final DocumentChunkSearchStore documentChunkSearchStore;

    @Override
    @Transactional(readOnly = true)
    public List<SimilarDocumentChunk> findSimilarChunks(Long documentId, String queryVector, int topK) {
        validateTopK(topK);

        RagDocument document = ragDocumentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));

        if (document.getStatus() != DocumentStatus.READY) {
            throw new IllegalStateException("Document must be READY before retrieval. Current status: " + document.getStatus());
        }

        long embeddedChunkCount = documentChunkSearchStore.countEmbeddedChunks(documentId);

        if (embeddedChunkCount == 0) {
            throw new IllegalStateException("Document has no embedded chunks. Generate embeddings first for document: " + documentId);
        }

        return documentChunkSearchStore.findSimilarChunks(documentId, queryVector, topK);
    }

    private void validateTopK(int topK) {
        if (topK < 1 || topK > 20) {
            throw new IllegalArgumentException("topK must be between 1 and 20");
        }
    }
}