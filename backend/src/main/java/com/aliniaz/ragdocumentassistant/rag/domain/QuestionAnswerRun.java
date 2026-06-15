package com.aliniaz.ragdocumentassistant.rag.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Getter
@Entity
@Table(name = "question_answer_runs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionAnswerRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;

    @Enumerated(EnumType.STRING)
    @Column(name = "answer_status", nullable = false, length = 40)
    private AnswerStatus answerStatus;

    @Column(name = "model_name", length = 100)
    private String modelName;

    @Column(name = "embedding_model_name", length = 100)
    private String embeddingModelName;

    @Column(name = "top_k", nullable = false)
    private Integer topK;

    @Column(name = "raw_prompt", columnDefinition = "TEXT")
    private String rawPrompt;

    @Column(name = "raw_model_output", columnDefinition = "TEXT")
    private String rawModelOutput;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public QuestionAnswerRun(
            Long documentId,
            String question,
            String modelName,
            String embeddingModelName,
            Integer topK,
            String rawPrompt
    ) {
        this.documentId = documentId;
        this.question = question;
        this.modelName = modelName;
        this.embeddingModelName = embeddingModelName;
        this.topK = topK;
        this.rawPrompt = rawPrompt;
        this.answerStatus = AnswerStatus.FAILED;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public void markAnswered(String answer, String rawModelOutput) {
        this.answerStatus = AnswerStatus.ANSWERED;
        this.answer = answer;
        this.rawModelOutput = rawModelOutput;
        this.failureReason = null;
    }

    public void markInsufficientContext(String answer, String rawModelOutput) {
        this.answerStatus = AnswerStatus.INSUFFICIENT_CONTEXT;
        this.answer = answer;
        this.rawModelOutput = rawModelOutput;
        this.failureReason = null;
    }

    public void markFailed(String failureReason) {
        this.answerStatus = AnswerStatus.FAILED;
        this.failureReason = failureReason;
    }
}