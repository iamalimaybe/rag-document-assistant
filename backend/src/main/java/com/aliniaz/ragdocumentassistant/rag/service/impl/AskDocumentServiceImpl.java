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
import com.aliniaz.ragdocumentassistant.rag.api.response.RetrievedChunkResponse;
import com.aliniaz.ragdocumentassistant.rag.domain.AnswerStatus;
import com.aliniaz.ragdocumentassistant.rag.domain.QuestionAnswerRun;
import com.aliniaz.ragdocumentassistant.rag.domain.RetrievedChunk;
import com.aliniaz.ragdocumentassistant.rag.repository.QuestionAnswerRunRepository;
import com.aliniaz.ragdocumentassistant.rag.repository.RetrievedChunkRepository;
import com.aliniaz.ragdocumentassistant.rag.service.AskDocumentService;
import com.aliniaz.ragdocumentassistant.rag.service.GroundedAnswerModelOutput;
import com.aliniaz.ragdocumentassistant.rag.service.GroundedAnswerPromptBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AskDocumentServiceImpl implements AskDocumentService {

    private static final int DEFAULT_TOP_K = 5;

    private final EmbeddingClient embeddingClient;
    private final VectorFormatter vectorFormatter;
    private final EmbeddingProperties embeddingProperties;
    private final LlmProperties llmProperties;
    private final LlmClient llmClient;
    private final DocumentChunkSearchService documentChunkSearchService;
    private final GroundedAnswerPromptBuilder groundedAnswerPromptBuilder;
    private final QuestionAnswerRunRepository questionAnswerRunRepository;
    private final RetrievedChunkRepository retrievedChunkRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public AskDocumentResponse ask(Long documentId, AskDocumentRequest request) {
        int topK = request.topK() == null ? DEFAULT_TOP_K : request.topK();

        List<Double> questionEmbedding = embeddingClient.embed(request.question());
        String questionVector = vectorFormatter.toVector(questionEmbedding);

        List<SimilarDocumentChunk> chunks = documentChunkSearchService.findSimilarChunks(
                documentId,
                questionVector,
                topK
        );

        String prompt = groundedAnswerPromptBuilder.buildPrompt(request.question(), chunks);

        QuestionAnswerRun qaRun = new QuestionAnswerRun(
                documentId,
                request.question(),
                llmProperties.model(),
                embeddingProperties.model(),
                topK,
                prompt
        );

        qaRun = questionAnswerRunRepository.save(qaRun);
        saveRetrievedChunks(qaRun.getId(), chunks);

        GroundedAnswerModelOutput modelOutput = null;
        String rawModelOutput = null;

        try {
            LlmGenerationResponse generation = llmClient.generate(new LlmGenerationRequest(prompt));
            rawModelOutput = generation.rawOutput();

            modelOutput = parseModelOutput(rawModelOutput, chunks);

            if (modelOutput.answerStatus() == AnswerStatus.INSUFFICIENT_CONTEXT) {
                qaRun.markInsufficientContext(modelOutput.answer(), rawModelOutput);
            } else {
                qaRun.markAnswered(modelOutput.answer(), rawModelOutput);
            }
        } catch (Exception e) {
            qaRun.markFailed(rootMessage(e), rawModelOutput);
        }

        QuestionAnswerRun savedRun = questionAnswerRunRepository.save(qaRun);

        List<RetrievedChunkResponse> retrievedChunkResponses = chunks.stream()
                .map(RetrievedChunkResponse::from)
                .toList();

        List<RetrievedChunkResponse> citationResponses = buildCitationResponses(
                savedRun,
                modelOutput,
                chunks
        );

        return new AskDocumentResponse(
                savedRun.getId(),
                documentId,
                request.question(),
                savedRun.getAnswer(),
                savedRun.getAnswerStatus(),
                savedRun.getFailureReason(),
                savedRun.getModelName(),
                savedRun.getEmbeddingModelName(),
                savedRun.getTopK(),
                citationResponses,
                retrievedChunkResponses
        );
    }

    private GroundedAnswerModelOutput parseModelOutput(String rawOutput, List<SimilarDocumentChunk> retrievedChunks) {
        try {
            GroundedAnswerModelOutput output = objectMapper.readValue(rawOutput, GroundedAnswerModelOutput.class);

            if (output.answerStatus() == null) {
                throw new IllegalArgumentException("Model output is missing answer_status");
            }

            if (output.answerStatus() == AnswerStatus.FAILED) {
                throw new IllegalArgumentException("Model output must not return FAILED answer_status");
            }

            if (output.answer() == null || output.answer().isBlank()) {
                throw new IllegalArgumentException("Model output is missing answer");
            }

            validateCitations(output, retrievedChunks);

            return output;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse grounded answer model output", e);
        }
    }

    private void validateCitations(GroundedAnswerModelOutput output, List<SimilarDocumentChunk> retrievedChunks) {
        if (output.answerStatus() == AnswerStatus.INSUFFICIENT_CONTEXT) {
            return;
        }

        if (output.citations() == null || output.citations().isEmpty()) {
            throw new IllegalArgumentException("ANSWERED model output must include at least one citation");
        }

        Set<Long> retrievedChunkIds = new LinkedHashSet<>(
                retrievedChunks.stream()
                        .map(SimilarDocumentChunk::chunkId)
                        .toList()
        );

        List<Long> invalidCitations = output.citations()
                .stream()
                .filter(citation -> !retrievedChunkIds.contains(citation))
                .toList();

        if (!invalidCitations.isEmpty()) {
            throw new IllegalArgumentException("Model cited chunks that were not retrieved: " + invalidCitations);
        }
    }

    private List<RetrievedChunkResponse> buildCitationResponses(
            QuestionAnswerRun savedRun,
            GroundedAnswerModelOutput modelOutput,
            List<SimilarDocumentChunk> retrievedChunks
    ) {
        if (savedRun.getAnswerStatus() != AnswerStatus.ANSWERED || modelOutput == null || modelOutput.citations() == null) {
            return List.of();
        }

        Set<Long> citedChunkIds = new LinkedHashSet<>(modelOutput.citations());

        return retrievedChunks.stream()
                .filter(chunk -> citedChunkIds.contains(chunk.chunkId()))
                .map(RetrievedChunkResponse::from)
                .toList();
    }

    private void saveRetrievedChunks(Long qaRunId, List<SimilarDocumentChunk> chunks) {
        List<RetrievedChunk> retrievedChunks = chunks.stream()
                .map(chunk -> new RetrievedChunk(
                        qaRunId,
                        chunk.chunkId(),
                        chunk.chunkIndex() + 1,
                        chunk.similarityScore(),
                        chunk.content(),
                        chunk.pageNumber()
                ))
                .toList();

        retrievedChunkRepository.saveAll(retrievedChunks);
    }

    private String rootMessage(Exception e) {
        Throwable current = e;

        while (current.getCause() != null) {
            current = current.getCause();
        }

        return current.getMessage() == null ? "Answer generation failed" : current.getMessage();
    }
}