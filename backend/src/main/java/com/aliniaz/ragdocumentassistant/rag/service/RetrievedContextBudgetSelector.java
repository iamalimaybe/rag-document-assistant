package com.aliniaz.ragdocumentassistant.rag.service;

import com.aliniaz.ragdocumentassistant.document.service.SimilarDocumentChunk;

import java.util.List;

public interface RetrievedContextBudgetSelector {

    List<SimilarDocumentChunk> selectForPrompt(List<SimilarDocumentChunk> retrievedChunks);
}