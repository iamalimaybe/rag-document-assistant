package com.aliniaz.ragdocumentassistant.rag.api.response;

import com.aliniaz.ragdocumentassistant.rag.domain.AnswerStatus;
import com.aliniaz.ragdocumentassistant.rag.domain.QuestionAnswerRun;

import java.time.LocalDateTime;

public record QuestionAnswerRunSummaryResponse(
        Long id,
        Long documentId,
        String question,
        AnswerStatus answerStatus,
        String modelName,
        String embeddingModelName,
        Integer topK,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static QuestionAnswerRunSummaryResponse from(QuestionAnswerRun run) {
        return new QuestionAnswerRunSummaryResponse(
                run.getId(),
                run.getDocumentId(),
                run.getQuestion(),
                run.getAnswerStatus(),
                run.getModelName(),
                run.getEmbeddingModelName(),
                run.getTopK(),
                run.getCreatedAt(),
                run.getUpdatedAt()
        );
    }
}