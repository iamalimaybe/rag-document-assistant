package com.aliniaz.ragdocumentassistant.document.service.impl;

import com.aliniaz.ragdocumentassistant.document.config.ChunkingProperties;
import com.aliniaz.ragdocumentassistant.document.config.DocumentChunkingStrategy;
import com.aliniaz.ragdocumentassistant.document.service.DocumentChunkData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StructureAwareDocumentChunkerTest {

    private final StructureAwareDocumentChunker chunker = new StructureAwareDocumentChunker(
            text -> text == null ? "" : text.trim(),
            new ChunkingProperties(DocumentChunkingStrategy.STRUCTURE_AWARE, 80, 10),
            content -> Math.max(1, (int) Math.ceil((double) content.length() / 4))
    );

    @Test
    void chunkReturnsEmptyListForNullText() {
        List<DocumentChunkData> chunks = chunker.chunk(null);

        assertTrue(chunks.isEmpty());
    }

    @Test
    void chunkReturnsEmptyListForBlankText() {
        List<DocumentChunkData> chunks = chunker.chunk("   \n\n   ");

        assertTrue(chunks.isEmpty());
    }

    @Test
    void chunkKeepsSmallParagraphBlocksTogetherWhenTheyFit() {
        String text = """
                Heading

                First paragraph.

                Second paragraph.
                """;

        List<DocumentChunkData> chunks = chunker.chunk(text);

        assertEquals(1, chunks.size());
        assertEquals("Heading\n\nFirst paragraph.\n\nSecond paragraph.", chunks.get(0).content());
    }

    @Test
    void chunkSplitsOnParagraphBoundariesWhenCombinedBlockWouldExceedLimit() {
        StructureAwareDocumentChunker smallChunker = new StructureAwareDocumentChunker(
                text -> text == null ? "" : text.trim(),
                new ChunkingProperties(DocumentChunkingStrategy.STRUCTURE_AWARE, 60, 10),
                content -> Math.max(1, (int) Math.ceil((double) content.length() / 4))
        );

        String text = """
            First paragraph has enough text.

            Second paragraph also has enough text.

            Third paragraph also has enough text.
            """;

        List<DocumentChunkData> chunks = smallChunker.chunk(text);

        assertEquals(3, chunks.size());
        assertEquals("First paragraph has enough text.", chunks.get(0).content());
        assertEquals("Second paragraph also has enough text.", chunks.get(1).content());
        assertEquals("Third paragraph also has enough text.", chunks.get(2).content());
    }

    @Test
    void chunkSplitsOversizedBlockUsingConfiguredSizeAndOverlap() {
        StructureAwareDocumentChunker smallChunker = new StructureAwareDocumentChunker(
                text -> text == null ? "" : text.trim(),
                new ChunkingProperties(DocumentChunkingStrategy.STRUCTURE_AWARE, 10, 2),
                content -> Math.max(1, (int) Math.ceil((double) content.length() / 5))
        );

        List<DocumentChunkData> chunks = smallChunker.chunk("abcdefghijklmnopqrst");

        assertEquals(3, chunks.size());
        assertEquals("abcdefghij", chunks.get(0).content());
        assertEquals("ijklmnopqr", chunks.get(1).content());
        assertEquals("qrst", chunks.get(2).content());
    }

    @Test
    void chunkCreatesStableIndexesHashesAndTokenEstimates() {
        StructureAwareDocumentChunker smallChunker = new StructureAwareDocumentChunker(
                text -> text == null ? "" : text.trim(),
                new ChunkingProperties(DocumentChunkingStrategy.STRUCTURE_AWARE, 60, 10),
                content -> Math.max(1, (int) Math.ceil((double) content.length() / 4))
        );

        List<DocumentChunkData> chunks = smallChunker.chunk("""
            First paragraph has enough text.

            Second paragraph also has enough text.
            """);

        assertEquals(2, chunks.size());

        assertEquals(0, chunks.get(0).chunkIndex());
        assertEquals(1, chunks.get(1).chunkIndex());

        assertNull(chunks.get(0).pageNumber());
        assertNotNull(chunks.get(0).contentHash());
        assertEquals(64, chunks.get(0).contentHash().length());

        assertTrue(chunks.get(0).tokenEstimate() > 0);
        assertTrue(chunks.get(1).tokenEstimate() > 0);
    }
}