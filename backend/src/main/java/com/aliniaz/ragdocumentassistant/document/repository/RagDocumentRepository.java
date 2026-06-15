package com.aliniaz.ragdocumentassistant.document.repository;

import com.aliniaz.ragdocumentassistant.document.domain.RagDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RagDocumentRepository extends JpaRepository<RagDocument, Long> {
}