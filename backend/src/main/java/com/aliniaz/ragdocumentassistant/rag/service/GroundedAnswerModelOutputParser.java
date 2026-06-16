package com.aliniaz.ragdocumentassistant.rag.service;

import com.aliniaz.ragdocumentassistant.document.service.SimilarDocumentChunk;

import java.util.List;

public interface GroundedAnswerModelOutputParser {

    GroundedAnswerModelOutput parse(String rawOutput, List<SimilarDocumentChunk> retrievedChunks);
}