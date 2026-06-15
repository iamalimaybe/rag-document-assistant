package com.aliniaz.ragdocumentassistant.rag.repository;

import com.aliniaz.ragdocumentassistant.rag.domain.RetrievedChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RetrievedChunkRepository extends JpaRepository<RetrievedChunk, Long> {

    List<RetrievedChunk> findByQaRunIdOrderByRankAsc(Long qaRunId);
}