package com.aliniaz.ragdocumentassistant.document.service.impl;

import com.aliniaz.ragdocumentassistant.document.config.ChunkingProperties;
import com.aliniaz.ragdocumentassistant.document.service.DocumentChunkData;
import com.aliniaz.ragdocumentassistant.document.service.DocumentTextNormalizer;
import com.aliniaz.ragdocumentassistant.document.service.TokenEstimator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StructureAwareDocumentChunker {

    private final DocumentTextNormalizer documentTextNormalizer;
    private final ChunkingProperties chunkingProperties;
    private final TokenEstimator tokenEstimator;

    public List<DocumentChunkData> chunk(String text) {
        String normalized = documentTextNormalizer.normalize(text);

        if (normalized.isBlank()) {
            return List.of();
        }

        List<String> blocks = Arrays.stream(normalized.split("\\n\\s*\\n"))
                .map(String::trim)
                .filter(block -> !block.isBlank())
                .toList();

        List<String> chunkContents = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();

        for (String block : blocks) {
            if (block.length() > chunkingProperties.chunkSizeChars()) {
                flushCurrentChunk(chunkContents, currentChunk);
                chunkContents.addAll(splitOversizedBlock(block));
                continue;
            }

            if (currentChunk.isEmpty()) {
                currentChunk.append(block);
                continue;
            }

            String candidate = currentChunk + "\n\n" + block;

            if (candidate.length() <= chunkingProperties.chunkSizeChars()) {
                currentChunk.append("\n\n").append(block);
            } else {
                flushCurrentChunk(chunkContents, currentChunk);
                currentChunk.append(block);
            }
        }

        flushCurrentChunk(chunkContents, currentChunk);

        List<DocumentChunkData> chunks = new ArrayList<>();

        for (int i = 0; i < chunkContents.size(); i++) {
            String content = chunkContents.get(i).trim();

            if (!content.isBlank()) {
                chunks.add(new DocumentChunkData(
                        i,
                        null,
                        content,
                        sha256(i + ":" + content),
                        tokenEstimator.estimate(content)
                ));
            }
        }

        return chunks;
    }

    private void flushCurrentChunk(List<String> chunkContents, StringBuilder currentChunk) {
        if (!currentChunk.isEmpty()) {
            chunkContents.add(currentChunk.toString().trim());
            currentChunk.setLength(0);
        }
    }

    private List<String> splitOversizedBlock(String block) {
        List<String> splitChunks = new ArrayList<>();
        int start = 0;

        while (start < block.length()) {
            int end = Math.min(start + chunkingProperties.chunkSizeChars(), block.length());
            String chunkContent = block.substring(start, end).trim();

            if (!chunkContent.isBlank()) {
                splitChunks.add(chunkContent);
            }

            if (end == block.length()) {
                break;
            }

            start = Math.max(0, end - chunkingProperties.chunkOverlapChars());
        }

        return splitChunks;
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