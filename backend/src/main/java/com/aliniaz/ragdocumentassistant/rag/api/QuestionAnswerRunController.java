package com.aliniaz.ragdocumentassistant.rag.api;

import com.aliniaz.ragdocumentassistant.rag.api.response.QuestionAnswerRunDetailResponse;
import com.aliniaz.ragdocumentassistant.rag.api.response.QuestionAnswerRunSummaryResponse;
import com.aliniaz.ragdocumentassistant.rag.service.QuestionAnswerRunQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class QuestionAnswerRunController {

    private final QuestionAnswerRunQueryService questionAnswerRunQueryService;

    @GetMapping("/api/qa-runs/{id}")
    public QuestionAnswerRunDetailResponse findById(@PathVariable Long id) {
        return questionAnswerRunQueryService.findById(id);
    }

    @GetMapping("/api/documents/{documentId}/qa-runs")
    public List<QuestionAnswerRunSummaryResponse> findByDocumentId(@PathVariable Long documentId) {
        return questionAnswerRunQueryService.findByDocumentId(documentId);
    }
}