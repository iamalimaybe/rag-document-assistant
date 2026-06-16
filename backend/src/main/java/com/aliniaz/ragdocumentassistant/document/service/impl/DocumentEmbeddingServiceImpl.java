package com.aliniaz.ragdocumentassistant.document.service.impl;

import com.aliniaz.ragdocumentassistant.common.exception.ResourceNotFoundException;
import com.aliniaz.ragdocumentassistant.document.domain.DocumentChunk;
import com.aliniaz.ragdocumentassistant.document.domain.DocumentStatus;
import com.aliniaz.ragdocumentassistant.document.domain.RagDocument;
import com.aliniaz.ragdocumentassistant.document.repository.DocumentChunkEmbeddingStore;
import com.aliniaz.ragdocumentassistant.document.repository.DocumentChunkRepository;
import com.aliniaz.ragdocumentassistant.document.repository.RagDocumentRepository;
import com.aliniaz.ragdocumentassistant.document.service.DocumentEmbeddingService;
import com.aliniaz.ragdocumentassistant.document.service.EmbeddedDocumentResponse;
import com.aliniaz.ragdocumentassistant.embedding.service.EmbeddingClient;
import com.aliniaz.ragdocumentassistant.embedding.service.VectorFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentEmbeddingServiceImpl implements DocumentEmbeddingService {

    private final RagDocumentRepository ragDocumentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final EmbeddingClient embeddingClient;
    private final VectorFormatter vectorFormatter;
    private final DocumentChunkEmbeddingStore documentChunkEmbeddingStore;

    @Override
    @Transactional
    public EmbeddedDocumentResponse embedDocumentChunks(Long documentId) {
        RagDocument document = ragDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));

        if (document.getStatus() != DocumentStatus.READY) {
            throw new IllegalStateException("Document must be READY before embedding. Current status: " + document.getStatus());
        }

        List<DocumentChunk> chunks = documentChunkRepository.findByDocumentIdOrderByChunkIndexAsc(documentId);

        if (chunks.isEmpty()) {
            throw new IllegalStateException("Document has no chunks to embed: " + documentId);
        }

        int embeddedCount = 0;

        for (DocumentChunk chunk : chunks) {
            List<Double> embedding = embeddingClient.embed(chunk.getContent());
            String vector = vectorFormatter.toVector(embedding);
            documentChunkEmbeddingStore.updateEmbedding(chunk.getId(), vector);
            embeddedCount++;
        }

        return new EmbeddedDocumentResponse(documentId, embeddedCount);
    }
}