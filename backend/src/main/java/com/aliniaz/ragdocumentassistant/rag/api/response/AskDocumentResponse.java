package com.aliniaz.ragdocumentassistant.rag.api.response;

import com.aliniaz.ragdocumentassistant.rag.domain.AnswerStatus;

import java.util.List;

public record AskDocumentResponse(
        Long qaRunId,
        Long documentId,
        String question,
        String answer,
        AnswerStatus answerStatus,
        String failureReason,
        String modelName,
        String embeddingModelName,
        Integer topK,
        // Chunks the model cited in structured JSON
        List<RetrievedChunkResponse> citations,
        // All chunks retrieved and stored as context snapshots
        List<RetrievedChunkResponse> retrievedChunks
) {
}