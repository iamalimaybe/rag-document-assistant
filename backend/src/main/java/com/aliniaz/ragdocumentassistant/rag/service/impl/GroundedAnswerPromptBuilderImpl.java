package com.aliniaz.ragdocumentassistant.rag.service.impl;

import com.aliniaz.ragdocumentassistant.document.service.SimilarDocumentChunk;
import com.aliniaz.ragdocumentassistant.rag.service.GroundedAnswerPromptBuilder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GroundedAnswerPromptBuilderImpl implements GroundedAnswerPromptBuilder {

    @Override
    public String buildPrompt(String question, List<SimilarDocumentChunk> chunks) {
        StringBuilder context = new StringBuilder();

        for (SimilarDocumentChunk chunk : chunks) {
            context.append("[chunk_id=")
                    .append(chunk.chunkId())
                    .append(", chunk_index=")
                    .append(chunk.chunkIndex())
                    .append(", page=")
                    .append(chunk.pageNumber() == null ? "unknown" : chunk.pageNumber())
                    .append("]\n")
                    .append(chunk.content())
                    .append("\n\n");
        }

        return """
                You are a grounded document assistant.

                Answer the user's question using only the provided document context.

                Rules:
                - Do not use outside knowledge.
                - If the context does not contain the answer, set answer_status to "INSUFFICIENT_CONTEXT".
                - If the context contains the answer, set answer_status to "ANSWERED".
                - Cite only chunk ids that directly support the answer.
                - Return only valid JSON. Do not include markdown. Do not include explanation outside JSON.

                Required JSON format:
                {
                  "answer_status": "ANSWERED",
                  "answer": "Short answer based only on the context.",
                  "citations": [1]
                }

                If context is insufficient, use:
                {
                  "answer_status": "INSUFFICIENT_CONTEXT",
                  "answer": "I do not have enough information in the provided document context to answer this.",
                  "citations": []
                }

                User question:
                %s

                Document context:
                %s
                """.formatted(question, context.toString().trim());
    }
}