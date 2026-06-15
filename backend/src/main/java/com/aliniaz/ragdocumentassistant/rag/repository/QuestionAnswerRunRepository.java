package com.aliniaz.ragdocumentassistant.rag.repository;

import com.aliniaz.ragdocumentassistant.rag.domain.QuestionAnswerRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionAnswerRunRepository extends JpaRepository<QuestionAnswerRun, Long> {

    List<QuestionAnswerRun> findByDocumentIdOrderByCreatedAtDesc(Long documentId);
}