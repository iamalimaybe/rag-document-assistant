package com.aliniaz.ragdocumentassistant.rag.service.impl;

import com.aliniaz.ragdocumentassistant.document.service.SimilarDocumentChunk;
import com.aliniaz.ragdocumentassistant.rag.config.RagContextBudgetProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TokenBudgetRetrievedContextSelectorTest {

    @Test
    void selectForPromptKeepsChunksWithinTokenBudget() {
        TokenBudgetRetrievedContextSelector selector = new TokenBudgetRetrievedContextSelector(
                new RagContextBudgetProperties(21)
        );

        List<SimilarDocumentChunk> selectedChunks = selector.selectForPrompt(retrievedChunks());

        assertEquals(2, selectedChunks.size());
        assertEquals(10L, selectedChunks.get(0).chunkId());
        assertEquals(20L, selectedChunks.get(1).chunkId());
    }

    @Test
    void selectForPromptPreservesRetrievalOrderForSelectedChunks() {
        TokenBudgetRetrievedContextSelector selector = new TokenBudgetRetrievedContextSelector(
                new RagContextBudgetProperties(30)
        );

        List<SimilarDocumentChunk> selectedChunks = selector.selectForPrompt(retrievedChunks());

        assertEquals(List.of(10L, 20L, 30L), selectedChunks.stream()
                .map(SimilarDocumentChunk::chunkId)
                .toList());
    }

    @Test
    void selectForPromptSkipsChunkThatWouldExceedBudgetAndContinues() {
        TokenBudgetRetrievedContextSelector selector = new TokenBudgetRetrievedContextSelector(
                new RagContextBudgetProperties(10)
        );

        List<SimilarDocumentChunk> selectedChunks = selector.selectForPrompt(retrievedChunks());

        assertEquals(1, selectedChunks.size());
        assertEquals(20L, selectedChunks.get(0).chunkId());
    }

    @Test
    void selectForPromptReturnsEmptyListForNullInput() {
        TokenBudgetRetrievedContextSelector selector = new TokenBudgetRetrievedContextSelector(
                new RagContextBudgetProperties(10)
        );

        assertTrue(selector.selectForPrompt(null).isEmpty());
    }

    @Test
    void selectForPromptReturnsEmptyListForEmptyInput() {
        TokenBudgetRetrievedContextSelector selector = new TokenBudgetRetrievedContextSelector(
                new RagContextBudgetProperties(10)
        );

        assertTrue(selector.selectForPrompt(List.of()).isEmpty());
    }

    @Test
    void contextBudgetPropertiesRejectInvalidBudget() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new RagContextBudgetProperties(0)
        );

        assertEquals("maxPromptChunkTokens must be greater than 0", exception.getMessage());
    }

    private List<SimilarDocumentChunk> retrievedChunks() {
        return List.of(
                new SimilarDocumentChunk(10L, 1L, 0, null, "First chunk", "hash-10", 12, 0.91),
                new SimilarDocumentChunk(20L, 1L, 1, null, "Second chunk", "hash-20", 9, 0.84),
                new SimilarDocumentChunk(30L, 1L, 2, null, "Third chunk", "hash-30", 7, 0.77)
        );
    }
}