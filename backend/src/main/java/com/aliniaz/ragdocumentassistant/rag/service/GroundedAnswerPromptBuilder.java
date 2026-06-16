package com.aliniaz.ragdocumentassistant.rag.service;

import com.aliniaz.ragdocumentassistant.document.service.SimilarDocumentChunk;

import java.util.List;

public interface GroundedAnswerPromptBuilder {

    String buildPrompt(String question, List<SimilarDocumentChunk> chunks);
}