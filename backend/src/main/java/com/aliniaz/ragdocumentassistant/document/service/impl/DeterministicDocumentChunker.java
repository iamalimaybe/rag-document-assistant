package com.aliniaz.ragdocumentassistant.document.service.impl;

import com.aliniaz.ragdocumentassistant.document.config.ChunkingProperties;
import com.aliniaz.ragdocumentassistant.document.service.DocumentChunkData;
import com.aliniaz.ragdocumentassistant.document.service.DocumentChunker;
import com.aliniaz.ragdocumentassistant.document.service.DocumentTextNormalizer;
import com.aliniaz.ragdocumentassistant.document.service.TokenEstimator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeterministicDocumentChunker implements DocumentChunker {

    private final DocumentTextNormalizer documentTextNormalizer;
    private final ChunkingProperties chunkingProperties;
    private final TokenEstimator tokenEstimator;

    @Override
    public List<DocumentChunkData> chunk(String text) {
        String normalized = documentTextNormalizer.normalize(text);

        if (normalized.isBlank()) {
            return List.of();
        }

        List<DocumentChunkData> chunks = new ArrayList<>();
        int start = 0;
        int chunkIndex = 0;

        while (start < normalized.length()) {
            int end = Math.min(start + chunkingProperties.chunkSizeChars(), normalized.length());
            String chunkContent = normalized.substring(start, end).trim();

            if (!chunkContent.isBlank()) {
                chunks.add(new DocumentChunkData(
                        chunkIndex,
                        null,
                        chunkContent,
                        sha256(chunkIndex + ":" + chunkContent),
                        tokenEstimator.estimate(chunkContent)
                ));
                chunkIndex++;
            }

            if (end == normalized.length()) {
                break;
            }

            start = Math.max(0, end - chunkingProperties.chunkOverlapChars());
        }

        return chunks;
    }

    private String sha256(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }
}