package com.aliniaz.ragdocumentassistant.rag.service.impl;

import com.aliniaz.ragdocumentassistant.document.service.DocumentChunkSearchService;
import com.aliniaz.ragdocumentassistant.document.service.SimilarDocumentChunk;
import com.aliniaz.ragdocumentassistant.embedding.config.EmbeddingProperties;
import com.aliniaz.ragdocumentassistant.embedding.service.EmbeddingClient;
import com.aliniaz.ragdocumentassistant.embedding.service.VectorFormatter;
import com.aliniaz.ragdocumentassistant.rag.api.request.RetrievalTestRequest;
import com.aliniaz.ragdocumentassistant.rag.api.response.RetrievalTestResponse;
import com.aliniaz.ragdocumentassistant.rag.api.response.RetrievedChunkResponse;
import com.aliniaz.ragdocumentassistant.rag.service.RetrievalTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RetrievalTestServiceImpl implements RetrievalTestService {

    private static final int DEFAULT_TOP_K = 5;

    private final EmbeddingClient embeddingClient;
    private final VectorFormatter vectorFormatter;
    private final EmbeddingProperties embeddingProperties;
    private final DocumentChunkSearchService documentChunkSearchService;

    @Override
    public RetrievalTestResponse retrieve(Long documentId, RetrievalTestRequest request) {
        int topK = request.topK() == null ? DEFAULT_TOP_K : request.topK();

        List<Double> questionEmbedding = embeddingClient.embed(request.question());
        String questionVector = vectorFormatter.toVector(questionEmbedding);

        List<SimilarDocumentChunk> similarChunks = documentChunkSearchService.findSimilarChunks(
                documentId,
                questionVector,
                topK
        );

        List<RetrievedChunkResponse> chunks = similarChunks.stream()
                .map(RetrievedChunkResponse::from)
                .toList();

        return new RetrievalTestResponse(
                documentId,
                request.question(),
                topK,
                embeddingProperties.model(),
                chunks
        );
    }
}