package com.aliniaz.ragdocumentassistant.document.repository.impl;

import com.aliniaz.ragdocumentassistant.document.repository.DocumentChunkSearchStore;
import com.aliniaz.ragdocumentassistant.document.service.SimilarDocumentChunk;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostgresDocumentChunkSearchStore implements DocumentChunkSearchStore {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<SimilarDocumentChunk> findSimilarChunks(Long documentId, String queryVector, int topK) {
        return jdbcTemplate.query(
                """
                SELECT
                    id,
                    document_id,
                    chunk_index,
                    page_number,
                    content,
                    content_hash,
                    token_estimate,
                    1 - (embedding <=> CAST(? AS vector)) AS similarity_score
                FROM document_chunks
                WHERE document_id = ?
                  AND embedding IS NOT NULL
                ORDER BY embedding <=> CAST(? AS vector)
                LIMIT ?
                """,
                (rs, rowNum) -> new SimilarDocumentChunk(
                        rs.getLong("id"),
                        rs.getLong("document_id"),
                        rs.getInt("chunk_index"),
                        rs.getObject("page_number", Integer.class),
                        rs.getString("content"),
                        rs.getString("content_hash"),
                        rs.getObject("token_estimate", Integer.class),
                        rs.getDouble("similarity_score")
                ),
                queryVector,
                documentId,
                queryVector,
                topK
        );
    }

    @Override
    public long countEmbeddedChunks(Long documentId) {
        Long count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM document_chunks
                WHERE document_id = ?
                  AND embedding IS NOT NULL
                """,
                Long.class,
                documentId
        );

        return count == null ? 0 : count;
    }
}