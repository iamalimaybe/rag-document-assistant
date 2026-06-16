package com.aliniaz.ragdocumentassistant.rag.api.response;

import com.aliniaz.ragdocumentassistant.rag.domain.AnswerStatus;
import com.aliniaz.ragdocumentassistant.rag.domain.QuestionAnswerRun;

import java.time.LocalDateTime;
import java.util.List;

public record QuestionAnswerRunDetailResponse(
        Long id,
        Long documentId,
        String question,
        String answer,
        AnswerStatus answerStatus,
        String failureReason,
        String modelName,
        String embeddingModelName,
        Integer topK,
        String rawPrompt,
        String rawModelOutput,
        List<RetrievedChunkSnapshotResponse> retrievedChunks,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static QuestionAnswerRunDetailResponse from(
            QuestionAnswerRun run,
            List<RetrievedChunkSnapshotResponse> retrievedChunks
    ) {
        return new QuestionAnswerRunDetailResponse(
                run.getId(),
                run.getDocumentId(),
                run.getQuestion(),
                run.getAnswer(),
                run.getAnswerStatus(),
                run.getFailureReason(),
                run.getModelName(),
                run.getEmbeddingModelName(),
                run.getTopK(),
                run.getRawPrompt(),
                run.getRawModelOutput(),
                retrievedChunks,
                run.getCreatedAt(),
                run.getUpdatedAt()
        );
    }
}