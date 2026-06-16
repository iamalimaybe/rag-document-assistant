package com.aliniaz.ragdocumentassistant.rag.service;

import com.aliniaz.ragdocumentassistant.rag.api.response.QuestionAnswerRunDetailResponse;
import com.aliniaz.ragdocumentassistant.rag.api.response.QuestionAnswerRunSummaryResponse;

import java.util.List;

public interface QuestionAnswerRunQueryService {

    QuestionAnswerRunDetailResponse findById(Long id);

    List<QuestionAnswerRunSummaryResponse> findByDocumentId(Long documentId);
}