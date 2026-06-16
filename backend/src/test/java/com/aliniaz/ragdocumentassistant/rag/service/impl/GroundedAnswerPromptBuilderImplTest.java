package com.aliniaz.ragdocumentassistant.rag.service.impl;

import com.aliniaz.ragdocumentassistant.document.service.SimilarDocumentChunk;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GroundedAnswerPromptBuilderImplTest {

    private final GroundedAnswerPromptBuilderImpl promptBuilder = new GroundedAnswerPromptBuilderImpl();

    @Test
    void buildPromptIncludesGroundingRulesAndRequiredJsonShape() {
        String prompt = promptBuilder.buildPrompt(
                "What should RAG systems use to answer questions?",
                retrievedChunks()
        );

        assertTrue(prompt.contains("You are a grounded document assistant."));
        assertTrue(prompt.contains("Answer the user's question using only the provided document context."));
        assertTrue(prompt.contains("Do not use outside knowledge."));
        assertTrue(prompt.contains("Return only valid JSON."));
        assertTrue(prompt.contains("\"answer_status\": \"ANSWERED\""));
        assertTrue(prompt.contains("\"citations\": [1]"));
        assertTrue(prompt.contains("\"answer_status\": \"INSUFFICIENT_CONTEXT\""));
        assertTrue(prompt.contains("\"citations\": []"));
    }

    @Test
    void buildPromptIncludesUserQuestion() {
        String prompt = promptBuilder.buildPrompt(
                "What should RAG systems use to answer questions?",
                retrievedChunks()
        );

        assertTrue(prompt.contains("User question:"));
        assertTrue(prompt.contains("What should RAG systems use to answer questions?"));
    }

    @Test
    void buildPromptIncludesRetrievedChunkMetadataAndContent() {
        String prompt = promptBuilder.buildPrompt(
                "What should RAG systems use to answer questions?",
                retrievedChunks()
        );

        assertTrue(prompt.contains("[chunk_id=10, chunk_index=0, page=unknown]"));
        assertTrue(prompt.contains("RAG systems should answer using retrieved document context."));

        assertTrue(prompt.contains("[chunk_id=20, chunk_index=1, page=3]"));
        assertTrue(prompt.contains("Answers should cite supporting chunks."));
    }

    @Test
    void buildPromptUsesOnlyRetrievedChunkIdsAsCitationCandidates() {
        String prompt = promptBuilder.buildPrompt(
                "What should RAG systems use to answer questions?",
                retrievedChunks()
        );

        assertTrue(prompt.contains("Cite only chunk ids that directly support the answer."));
        assertTrue(prompt.contains("chunk_id=10"));
        assertTrue(prompt.contains("chunk_id=20"));
    }

    private List<SimilarDocumentChunk> retrievedChunks() {
        return List.of(
                new SimilarDocumentChunk(
                        10L,
                        1L,
                        0,
                        null,
                        "RAG systems should answer using retrieved document context.",
                        "hash-10",
                        12,
                        0.91
                ),
                new SimilarDocumentChunk(
                        20L,
                        1L,
                        1,
                        3,
                        "Answers should cite supporting chunks.",
                        "hash-20",
                        9,
                        0.84
                )
        );
    }
}