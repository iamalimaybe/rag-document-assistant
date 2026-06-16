package com.aliniaz.ragdocumentassistant.rag.service.impl;

import com.aliniaz.ragdocumentassistant.document.service.DocumentChunkSearchService;
import com.aliniaz.ragdocumentassistant.document.service.SimilarDocumentChunk;
import com.aliniaz.ragdocumentassistant.embedding.config.EmbeddingProperties;
import com.aliniaz.ragdocumentassistant.embedding.service.EmbeddingClient;
import com.aliniaz.ragdocumentassistant.embedding.service.VectorFormatter;
import com.aliniaz.ragdocumentassistant.llm.config.LlmProperties;
import com.aliniaz.ragdocumentassistant.llm.service.LlmClient;
import com.aliniaz.ragdocumentassistant.llm.service.LlmGenerationRequest;
import com.aliniaz.ragdocumentassistant.llm.service.LlmGenerationResponse;
import com.aliniaz.ragdocumentassistant.rag.api.request.AskDocumentRequest;
import com.aliniaz.ragdocumentassistant.rag.api.response.AskDocumentResponse;
import com.aliniaz.ragdocumentassistant.rag.domain.AnswerStatus;
import com.aliniaz.ragdocumentassistant.rag.domain.QuestionAnswerRun;
import com.aliniaz.ragdocumentassistant.rag.domain.RetrievedChunk;
import com.aliniaz.ragdocumentassistant.rag.repository.QuestionAnswerRunRepository;
import com.aliniaz.ragdocumentassistant.rag.repository.RetrievedChunkRepository;
import com.aliniaz.ragdocumentassistant.rag.service.GroundedAnswerModelOutput;
import com.aliniaz.ragdocumentassistant.rag.service.GroundedAnswerModelOutputParser;
import com.aliniaz.ragdocumentassistant.rag.service.GroundedAnswerPromptBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AskDocumentServiceImplTest {

    private EmbeddingClient embeddingClient;
    private VectorFormatter vectorFormatter;
    private LlmClient llmClient;
    private DocumentChunkSearchService documentChunkSearchService;
    private GroundedAnswerPromptBuilder groundedAnswerPromptBuilder;
    private QuestionAnswerRunRepository questionAnswerRunRepository;
    private RetrievedChunkRepository retrievedChunkRepository;
    private GroundedAnswerModelOutputParser groundedAnswerModelOutputParser;
    private AskDocumentServiceImpl service;

    @BeforeEach
    void setUp() {
        embeddingClient = mock(EmbeddingClient.class);
        vectorFormatter = mock(VectorFormatter.class);
        llmClient = mock(LlmClient.class);
        documentChunkSearchService = mock(DocumentChunkSearchService.class);
        groundedAnswerPromptBuilder = mock(GroundedAnswerPromptBuilder.class);
        questionAnswerRunRepository = mock(QuestionAnswerRunRepository.class);
        retrievedChunkRepository = mock(RetrievedChunkRepository.class);
        groundedAnswerModelOutputParser = mock(GroundedAnswerModelOutputParser.class);

        service = new AskDocumentServiceImpl(
                embeddingClient,
                vectorFormatter,
                new EmbeddingProperties("nomic-embed-text"),
                new LlmProperties("qwen3:4b", 0.0, 256, 4096),
                llmClient,
                documentChunkSearchService,
                groundedAnswerPromptBuilder,
                questionAnswerRunRepository,
                retrievedChunkRepository,
                groundedAnswerModelOutputParser
        );

        when(embeddingClient.embed(any())).thenReturn(List.of(0.1, 0.2, 0.3));
        when(vectorFormatter.toVector(any())).thenReturn("[0.1,0.2,0.3]");
        when(documentChunkSearchService.findSimilarChunks(any(), any(), anyInt())).thenReturn(retrievedChunks());
        when(groundedAnswerPromptBuilder.buildPrompt(any(), any())).thenReturn("grounded prompt");
        when(llmClient.generate(any(LlmGenerationRequest.class))).thenReturn(new LlmGenerationResponse("""
                {
                  "answer_status": "ANSWERED",
                  "answer": "RAG systems should answer using retrieved document context.",
                  "citations": [10]
                }
                """));

        when(questionAnswerRunRepository.save(any(QuestionAnswerRun.class))).thenAnswer(invocation -> {
            QuestionAnswerRun run = invocation.getArgument(0);

            if (run.getId() == null) {
                ReflectionTestUtils.setField(run, "id", 100L);
            }

            return run;
        });

        when(retrievedChunkRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void askReturnsAnsweredResponseWithOnlyCitedChunksAsCitationsAndAllChunksAsRetrievedChunks() {
        when(groundedAnswerModelOutputParser.parse(any(), any())).thenReturn(
                new GroundedAnswerModelOutput(
                        AnswerStatus.ANSWERED,
                        "RAG systems should answer using retrieved document context.",
                        List.of(10L)
                )
        );

        AskDocumentResponse response = service.ask(
                1L,
                new AskDocumentRequest("What should RAG systems use to answer questions?", 5)
        );

        assertEquals(100L, response.qaRunId());
        assertEquals(1L, response.documentId());
        assertEquals("What should RAG systems use to answer questions?", response.question());
        assertEquals("RAG systems should answer using retrieved document context.", response.answer());
        assertEquals(AnswerStatus.ANSWERED, response.answerStatus());
        assertNull(response.failureReason());
        assertEquals("qwen3:4b", response.modelName());
        assertEquals("nomic-embed-text", response.embeddingModelName());
        assertEquals(5, response.topK());

        assertEquals(1, response.citations().size());
        assertEquals(2, response.retrievedChunks().size());

        verify(retrievedChunkRepository).saveAll(any());
    }

    @Test
    void askReturnsInsufficientContextWithEmptyCitationsButKeepsRetrievedChunks() {
        when(groundedAnswerModelOutputParser.parse(any(), any())).thenReturn(
                new GroundedAnswerModelOutput(
                        AnswerStatus.INSUFFICIENT_CONTEXT,
                        "I do not have enough information in the provided document context to answer this.",
                        List.of()
                )
        );

        AskDocumentResponse response = service.ask(
                1L,
                new AskDocumentRequest("What is the refund period?", 5)
        );

        assertEquals(AnswerStatus.INSUFFICIENT_CONTEXT, response.answerStatus());
        assertEquals("I do not have enough information in the provided document context to answer this.", response.answer());
        assertNull(response.failureReason());

        assertTrue(response.citations().isEmpty());
        assertEquals(2, response.retrievedChunks().size());

        verify(retrievedChunkRepository).saveAll(any());
    }

    @Test
    void askMarksRunFailedWhenModelOutputParsingFails() {
        when(groundedAnswerModelOutputParser.parse(any(), any())).thenThrow(
                new IllegalArgumentException("Failed to parse grounded answer model output")
        );

        AskDocumentResponse response = service.ask(
                1L,
                new AskDocumentRequest("What should RAG systems use to answer questions?", 5)
        );

        assertEquals(AnswerStatus.FAILED, response.answerStatus());
        assertNull(response.answer());
        assertEquals("Failed to parse grounded answer model output", response.failureReason());

        assertTrue(response.citations().isEmpty());
        assertEquals(2, response.retrievedChunks().size());
    }

    @Test
    void askUsesDefaultTopKWhenRequestTopKIsNull() {
        when(groundedAnswerModelOutputParser.parse(any(), any())).thenReturn(
                new GroundedAnswerModelOutput(
                        AnswerStatus.ANSWERED,
                        "RAG systems should answer using retrieved document context.",
                        List.of(10L)
                )
        );

        AskDocumentResponse response = service.ask(
                1L,
                new AskDocumentRequest("What should RAG systems use to answer questions?", null)
        );

        assertEquals(5, response.topK());

        verify(documentChunkSearchService).findSimilarChunks(
                1L,
                "[0.1,0.2,0.3]",
                5
        );
    }

    @Test
    void askStoresRetrievedChunkSnapshotsUsingOneBasedRank() {
        when(groundedAnswerModelOutputParser.parse(any(), any())).thenReturn(
                new GroundedAnswerModelOutput(
                        AnswerStatus.ANSWERED,
                        "RAG systems should answer using retrieved document context.",
                        List.of(10L)
                )
        );

        service.ask(
                1L,
                new AskDocumentRequest("What should RAG systems use to answer questions?", 5)
        );

        ArgumentCaptor<List<RetrievedChunk>> captor = ArgumentCaptor.forClass(List.class);

        verify(retrievedChunkRepository).saveAll(captor.capture());

        List<RetrievedChunk> savedSnapshots = captor.getValue();

        assertEquals(2, savedSnapshots.size());
        assertEquals(100L, savedSnapshots.get(0).getQaRunId());
        assertEquals(10L, savedSnapshots.get(0).getDocumentChunkId());
        assertEquals(1, savedSnapshots.get(0).getRank());

        assertEquals(100L, savedSnapshots.get(1).getQaRunId());
        assertEquals(20L, savedSnapshots.get(1).getDocumentChunkId());
        assertEquals(2, savedSnapshots.get(1).getRank());
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