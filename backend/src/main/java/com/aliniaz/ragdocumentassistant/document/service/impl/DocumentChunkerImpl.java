package com.aliniaz.ragdocumentassistant.document.service.impl;

import com.aliniaz.ragdocumentassistant.document.service.DocumentChunkData;
import com.aliniaz.ragdocumentassistant.document.service.DocumentChunker;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

@Service
public class DocumentChunkerImpl implements DocumentChunker {

    private static final int CHUNK_SIZE_CHARS = 1200;
    private static final int CHUNK_OVERLAP_CHARS = 200;
    private static final int APPROX_CHARS_PER_TOKEN = 4;

    @Override
    public List<DocumentChunkData> chunk(String text) {
        String normalized = normalize(text);

        if (normalized.isBlank()) {
            return List.of();
        }

        List<DocumentChunkData> chunks = new ArrayList<>();
        int start = 0;
        int chunkIndex = 0;

        while (start < normalized.length()) {
            int end = Math.min(start + CHUNK_SIZE_CHARS, normalized.length());
            String chunkContent = normalized.substring(start, end).trim();

            if (!chunkContent.isBlank()) {
                chunks.add(new DocumentChunkData(
                        chunkIndex,
                        null,
                        chunkContent,
                        sha256(chunkIndex + ":" + chunkContent),
                        estimateTokens(chunkContent)
                ));
                chunkIndex++;
            }

            if (end == normalized.length()) {
                break;
            }

            start = Math.max(0, end - CHUNK_OVERLAP_CHARS);
        }

        return chunks;
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replace("\uFEFF", "")
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }

    private Integer estimateTokens(String content) {
        return Math.max(1, (int) Math.ceil((double) content.length() / APPROX_CHARS_PER_TOKEN));
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