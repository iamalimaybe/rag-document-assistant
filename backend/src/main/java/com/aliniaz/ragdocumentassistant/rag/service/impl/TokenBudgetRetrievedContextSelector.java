package com.aliniaz.ragdocumentassistant.rag.service.impl;

import com.aliniaz.ragdocumentassistant.document.service.SimilarDocumentChunk;
import com.aliniaz.ragdocumentassistant.rag.config.RagContextBudgetProperties;
import com.aliniaz.ragdocumentassistant.rag.service.RetrievedContextBudgetSelector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TokenBudgetRetrievedContextSelector implements RetrievedContextBudgetSelector {

    private final RagContextBudgetProperties ragContextBudgetProperties;

    @Override
    public List<SimilarDocumentChunk> selectForPrompt(List<SimilarDocumentChunk> retrievedChunks) {
        if (retrievedChunks == null || retrievedChunks.isEmpty()) {
            return List.of();
        }

        List<SimilarDocumentChunk> selectedChunks = new ArrayList<>();
        int usedTokens = 0;

        for (SimilarDocumentChunk chunk : retrievedChunks) {
            int chunkTokens = safeTokenEstimate(chunk);

            if (usedTokens + chunkTokens > ragContextBudgetProperties.maxPromptChunkTokens()) {
                continue;
            }

            selectedChunks.add(chunk);
            usedTokens += chunkTokens;
        }

        return selectedChunks;
    }

    private int safeTokenEstimate(SimilarDocumentChunk chunk) {
        if (chunk.tokenEstimate() == null || chunk.tokenEstimate() < 1) {
            return 1;
        }

        return chunk.tokenEstimate();
    }
}