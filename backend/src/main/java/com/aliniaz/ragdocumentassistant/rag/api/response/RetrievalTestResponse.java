package com.aliniaz.ragdocumentassistant.rag.api.response;

import java.util.List;

public record RetrievalTestResponse(
        Long documentId,
        String question,
        Integer topK,
        String embeddingModelName,
        List<RetrievedChunkResponse> chunks
) {
}