package com.aliniaz.ragdocumentassistant.document.service.impl;

import com.aliniaz.ragdocumentassistant.document.config.ChunkingProperties;
import com.aliniaz.ragdocumentassistant.document.service.DocumentChunkData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DocumentChunkerImplTest {

    private final DocumentChunkerImpl chunker = new DocumentChunkerImpl(
            text -> text == null ? "" : text.trim(),
            new ChunkingProperties(1200, 200),
            content -> Math.max(1, (int) Math.ceil((double) content.length() / 4))
    );

    @Test
    void chunkReturnsEmptyListForNullText() {
        List<DocumentChunkData> chunks = chunker.chunk(null);

        assertTrue(chunks.isEmpty());
    }

    @Test
    void chunkReturnsEmptyListForBlankText() {
        List<DocumentChunkData> chunks = chunker.chunk("   \n\t   ");

        assertTrue(chunks.isEmpty());
    }

    @Test
    void chunkCreatesSingleChunkForShortText() {
        List<DocumentChunkData> chunks = chunker.chunk("This is a short document.");

        assertEquals(1, chunks.size());

        DocumentChunkData chunk = chunks.get(0);

        assertEquals(0, chunk.chunkIndex());
        assertNull(chunk.pageNumber());
        assertEquals("This is a short document.", chunk.content());
        assertNotNull(chunk.contentHash());
        assertEquals(64, chunk.contentHash().length());
        assertEquals(7, chunk.tokenEstimate());
    }

    @Test
    void chunkCreatesMultipleChunksForLongTextWithStableIndexes() {
        String text = createDeterministicText(1500);

        List<DocumentChunkData> chunks = chunker.chunk(text);

        assertEquals(2, chunks.size());

        DocumentChunkData first = chunks.get(0);
        DocumentChunkData second = chunks.get(1);

        assertEquals(0, first.chunkIndex());
        assertEquals(1, second.chunkIndex());

        assertEquals(1200, first.content().length());
        assertEquals(500, second.content().length());

        assertFalse(first.content().isBlank());
        assertFalse(second.content().isBlank());

        assertEquals(300, first.tokenEstimate());
        assertEquals(125, second.tokenEstimate());
    }

    @Test
    void chunkOverlapsConsecutiveChunksByConfiguredOverlap() {
        String text = createDeterministicText(1500);

        List<DocumentChunkData> chunks = chunker.chunk(text);

        String firstChunkTail = chunks.get(0).content().substring(1000, 1200);
        String secondChunkHead = chunks.get(1).content().substring(0, 200);

        assertEquals(firstChunkTail, secondChunkHead);
    }

    @Test
    void chunkHashIncludesChunkIndexSoDuplicateContentAtDifferentIndexesGetsDifferentHash() {
        String repeatedContent = "a".repeat(2200);

        List<DocumentChunkData> chunks = chunker.chunk(repeatedContent);

        assertEquals(2, chunks.size());
        assertEquals(chunks.get(0).content(), chunks.get(1).content());
        assertNotEquals(chunks.get(0).contentHash(), chunks.get(1).contentHash());
    }

    @Test
    void chunkUsesConfiguredSizeOverlapAndTokenEstimate() {
        DocumentChunkerImpl configuredChunker = new DocumentChunkerImpl(
                text -> text == null ? "" : text.trim(),
                new ChunkingProperties(10, 2),
                content -> Math.max(1, (int) Math.ceil((double) content.length() / 5))
        );

        List<DocumentChunkData> chunks = configuredChunker.chunk("abcdefghijklmnopqrst");

        assertEquals(3, chunks.size());

        assertEquals("abcdefghij", chunks.get(0).content());
        assertEquals("ijklmnopqr", chunks.get(1).content());
        assertEquals("qrst", chunks.get(2).content());

        assertEquals(2, chunks.get(0).tokenEstimate());
        assertEquals(2, chunks.get(1).tokenEstimate());
        assertEquals(1, chunks.get(2).tokenEstimate());
    }

    @Test
    void chunkingPropertiesRejectOverlapEqualToChunkSize() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ChunkingProperties(10, 10)
        );

        assertEquals("chunkOverlapChars must be smaller than chunkSizeChars", exception.getMessage());
    }

    private String createDeterministicText(int length) {
        StringBuilder builder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            builder.append((char) ('a' + (i % 26)));
        }

        return builder.toString();
    }
}