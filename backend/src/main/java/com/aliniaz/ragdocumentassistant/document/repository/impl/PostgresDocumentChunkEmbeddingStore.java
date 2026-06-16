package com.aliniaz.ragdocumentassistant.document.repository.impl;

import com.aliniaz.ragdocumentassistant.document.repository.DocumentChunkEmbeddingStore;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostgresDocumentChunkEmbeddingStore implements DocumentChunkEmbeddingStore {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void updateEmbedding(Long chunkId, String vector) {
        jdbcTemplate.update(
                """
                UPDATE document_chunks
                SET embedding = CAST(? AS vector),
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """,
                vector,
                chunkId
        );
    }
}