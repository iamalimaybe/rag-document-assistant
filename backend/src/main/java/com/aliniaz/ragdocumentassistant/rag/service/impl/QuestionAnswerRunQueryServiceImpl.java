package com.aliniaz.ragdocumentassistant.rag.service.impl;

import com.aliniaz.ragdocumentassistant.rag.api.response.QuestionAnswerRunDetailResponse;
import com.aliniaz.ragdocumentassistant.rag.api.response.QuestionAnswerRunSummaryResponse;
import com.aliniaz.ragdocumentassistant.rag.api.response.RetrievedChunkSnapshotResponse;
import com.aliniaz.ragdocumentassistant.rag.domain.QuestionAnswerRun;
import com.aliniaz.ragdocumentassistant.rag.repository.QuestionAnswerRunRepository;
import com.aliniaz.ragdocumentassistant.rag.repository.RetrievedChunkRepository;
import com.aliniaz.ragdocumentassistant.rag.service.QuestionAnswerRunQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionAnswerRunQueryServiceImpl implements QuestionAnswerRunQueryService {

    private final QuestionAnswerRunRepository questionAnswerRunRepository;
    private final RetrievedChunkRepository retrievedChunkRepository;

    @Override
    @Transactional(readOnly = true)
    public QuestionAnswerRunDetailResponse findById(Long id) {
        QuestionAnswerRun run = questionAnswerRunRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Question answer run not found: " + id));

        List<RetrievedChunkSnapshotResponse> retrievedChunks = retrievedChunkRepository.findByQaRunIdOrderByRankAsc(id)
                .stream()
                .map(RetrievedChunkSnapshotResponse::from)
                .toList();

        return QuestionAnswerRunDetailResponse.from(run, retrievedChunks);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionAnswerRunSummaryResponse> findByDocumentId(Long documentId) {
        return questionAnswerRunRepository.findByDocumentIdOrderByCreatedAtDesc(documentId)
                .stream()
                .map(QuestionAnswerRunSummaryResponse::from)
                .toList();
    }
}