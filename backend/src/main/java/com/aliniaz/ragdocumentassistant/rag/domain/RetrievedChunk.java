package com.aliniaz.ragdocumentassistant.rag.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Getter
@Entity
@Table(name = "retrieved_chunks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RetrievedChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "qa_run_id", nullable = false)
    private Long qaRunId;

    @Column(name = "document_chunk_id")
    private Long documentChunkId;

    @Column(name = "rank", nullable = false)
    private Integer rank;

    @Column(name = "similarity_score", nullable = false)
    private Double similarityScore;

    @Column(name = "content_snapshot", nullable = false, columnDefinition = "TEXT")
    private String contentSnapshot;

    @Column(name = "page_number_snapshot")
    private Integer pageNumberSnapshot;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public RetrievedChunk(
            Long qaRunId,
            Long documentChunkId,
            Integer rank,
            Double similarityScore,
            String contentSnapshot,
            Integer pageNumberSnapshot
    ) {
        this.qaRunId = qaRunId;
        this.documentChunkId = documentChunkId;
        this.rank = rank;
        this.similarityScore = similarityScore;
        this.contentSnapshot = contentSnapshot;
        this.pageNumberSnapshot = pageNumberSnapshot;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}