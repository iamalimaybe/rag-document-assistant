package com.aliniaz.ragdocumentassistant.rag.service.impl;

import com.aliniaz.ragdocumentassistant.document.service.SimilarDocumentChunk;
import com.aliniaz.ragdocumentassistant.rag.domain.AnswerStatus;
import com.aliniaz.ragdocumentassistant.rag.service.GroundedAnswerModelOutput;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GroundedAnswerModelOutputParserImplTest {

    private final GroundedAnswerModelOutputParserImpl parser =
            new GroundedAnswerModelOutputParserImpl(new ObjectMapper());

    @Test
    void parseAllowsAnsweredOutputWithValidCitation() {
        String rawOutput = """
                {
                  "answer_status": "ANSWERED",
                  "answer": "RAG systems should answer using retrieved document context.",
                  "citations": [10]
                }
                """;

        GroundedAnswerModelOutput output = parser.parse(rawOutput, retrievedChunks());

        assertEquals(AnswerStatus.ANSWERED, output.answerStatus());
        assertEquals("RAG systems should answer using retrieved document context.", output.answer());
        assertEquals(List.of(10L), output.citations());
    }

    @Test
    void parseRejectsAnsweredOutputWithEmptyCitations() {
        String rawOutput = """
                {
                  "answer_status": "ANSWERED",
                  "answer": "RAG systems should answer using retrieved document context.",
                  "citations": []
                }
                """;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(rawOutput, retrievedChunks())
        );

        assertEquals("Failed to parse grounded answer model output", exception.getMessage());
        assertTrue(exception.getCause().getMessage().contains("ANSWERED model output must include at least one citation"));
    }

    @Test
    void parseRejectsAnsweredOutputWithMissingCitations() {
        String rawOutput = """
                {
                  "answer_status": "ANSWERED",
                  "answer": "RAG systems should answer using retrieved document context."
                }
                """;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(rawOutput, retrievedChunks())
        );

        assertEquals("Failed to parse grounded answer model output", exception.getMessage());
        assertTrue(exception.getCause().getMessage().contains("Model output is missing citations"));
    }

    @Test
    void parseRejectsAnsweredOutputWithCitationOutsideRetrievedChunks() {
        String rawOutput = """
                {
                  "answer_status": "ANSWERED",
                  "answer": "RAG systems should answer using retrieved document context.",
                  "citations": [999]
                }
                """;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(rawOutput, retrievedChunks())
        );

        assertEquals("Failed to parse grounded answer model output", exception.getMessage());
        assertTrue(exception.getCause().getMessage().contains("Model cited chunks that were not retrieved: [999]"));
    }

    @Test
    void parseAllowsInsufficientContextOutputWithEmptyCitations() {
        String rawOutput = """
                {
                  "answer_status": "INSUFFICIENT_CONTEXT",
                  "answer": "I do not have enough information in the provided document context to answer this.",
                  "citations": []
                }
                """;

        GroundedAnswerModelOutput output = parser.parse(rawOutput, retrievedChunks());

        assertEquals(AnswerStatus.INSUFFICIENT_CONTEXT, output.answerStatus());
        assertEquals("I do not have enough information in the provided document context to answer this.", output.answer());
        assertTrue(output.citations().isEmpty());
    }

    @Test
    void parseRejectsFailedAnswerStatusFromModel() {
        String rawOutput = """
                {
                  "answer_status": "FAILED",
                  "answer": "Something failed.",
                  "citations": []
                }
                """;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(rawOutput, retrievedChunks())
        );

        assertEquals("Failed to parse grounded answer model output", exception.getMessage());
        assertTrue(exception.getCause().getMessage().contains("Model output must not return FAILED answer_status"));
    }

    @Test
    void parseRejectsMissingAnswerStatus() {
        String rawOutput = """
                {
                  "answer": "RAG systems should answer using retrieved document context.",
                  "citations": [10]
                }
                """;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(rawOutput, retrievedChunks())
        );

        assertEquals("Failed to parse grounded answer model output", exception.getMessage());
        assertTrue(exception.getCause().getMessage().contains("Model output is missing answer_status"));
    }

    @Test
    void parseRejectsMissingAnswer() {
        String rawOutput = """
                {
                  "answer_status": "ANSWERED",
                  "citations": [10]
                }
                """;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(rawOutput, retrievedChunks())
        );

        assertEquals("Failed to parse grounded answer model output", exception.getMessage());
        assertTrue(exception.getCause().getMessage().contains("Model output is missing answer"));
    }

    @Test
    void parseRejectsBlankAnswer() {
        String rawOutput = """
                {
                  "answer_status": "ANSWERED",
                  "answer": "   ",
                  "citations": [10]
                }
                """;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(rawOutput, retrievedChunks())
        );

        assertEquals("Failed to parse grounded answer model output", exception.getMessage());
        assertTrue(exception.getCause().getMessage().contains("Model output is missing answer"));
    }

    @Test
    void parseRejectsMalformedJson() {
        String rawOutput = """
                {
                  "answer_status": "ANSWERED",
                  "answer": "RAG systems should answer using retrieved document context.",
                  "citations": [10]
                """;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(rawOutput, retrievedChunks())
        );

        assertEquals("Failed to parse grounded answer model output", exception.getMessage());
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
                        2,
                        "Answers should cite supporting chunks.",
                        "hash-20",
                        9,
                        0.84
                )
        );
    }
}