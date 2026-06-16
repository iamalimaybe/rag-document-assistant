package com.aliniaz.ragdocumentassistant.rag.service.impl;

import com.aliniaz.ragdocumentassistant.document.service.SimilarDocumentChunk;
import com.aliniaz.ragdocumentassistant.rag.domain.AnswerStatus;
import com.aliniaz.ragdocumentassistant.rag.service.GroundedAnswerModelOutput;
import com.aliniaz.ragdocumentassistant.rag.service.GroundedAnswerModelOutputParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class GroundedAnswerModelOutputParserImpl implements GroundedAnswerModelOutputParser {

    private final ObjectMapper objectMapper;

    @Override
    public GroundedAnswerModelOutput parse(String rawOutput, List<SimilarDocumentChunk> retrievedChunks) {
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

            if (output.citations() == null) {
                throw new IllegalArgumentException("Model output is missing citations");
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

        if (output.citations().isEmpty()) {
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
}