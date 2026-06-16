package com.aliniaz.ragdocumentassistant.document.service.impl;

import com.aliniaz.ragdocumentassistant.document.config.ChunkingProperties;
import com.aliniaz.ragdocumentassistant.document.config.DocumentChunkingStrategy;
import com.aliniaz.ragdocumentassistant.document.service.DocumentChunkData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ConfigurableDocumentChunkerTest {

    @Test
    void chunkDelegatesToDeterministicChunkerWhenStrategyIsDeterministic() {
        DeterministicDocumentChunker deterministicDocumentChunker = mock(DeterministicDocumentChunker.class);
        StructureAwareDocumentChunker structureAwareDocumentChunker = mock(StructureAwareDocumentChunker.class);

        List<DocumentChunkData> expectedChunks = List.of(
                new DocumentChunkData(0, null, "Document content", "hash", 3)
        );

        when(deterministicDocumentChunker.chunk("Document content")).thenReturn(expectedChunks);

        ConfigurableDocumentChunker chunker = new ConfigurableDocumentChunker(
                new ChunkingProperties(DocumentChunkingStrategy.DETERMINISTIC, 1200, 200),
                deterministicDocumentChunker,
                structureAwareDocumentChunker
        );

        List<DocumentChunkData> chunks = chunker.chunk("Document content");

        assertEquals(expectedChunks, chunks);
        verify(deterministicDocumentChunker).chunk("Document content");
        verifyNoInteractions(structureAwareDocumentChunker);
    }

    @Test
    void chunkDelegatesToStructureAwareChunkerWhenStrategyIsStructureAware() {
        DeterministicDocumentChunker deterministicDocumentChunker = mock(DeterministicDocumentChunker.class);
        StructureAwareDocumentChunker structureAwareDocumentChunker = mock(StructureAwareDocumentChunker.class);

        List<DocumentChunkData> expectedChunks = List.of(
                new DocumentChunkData(0, null, "Paragraph content", "hash", 4)
        );

        when(structureAwareDocumentChunker.chunk("Paragraph content")).thenReturn(expectedChunks);

        ConfigurableDocumentChunker chunker = new ConfigurableDocumentChunker(
                new ChunkingProperties(DocumentChunkingStrategy.STRUCTURE_AWARE, 1200, 200),
                deterministicDocumentChunker,
                structureAwareDocumentChunker
        );

        List<DocumentChunkData> chunks = chunker.chunk("Paragraph content");

        assertEquals(expectedChunks, chunks);
        verify(structureAwareDocumentChunker).chunk("Paragraph content");
        verifyNoInteractions(deterministicDocumentChunker);
    }

    @Test
    void chunkingPropertiesRejectMissingStrategy() {
        IllegalArgumentException exception = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ChunkingProperties(null, 1200, 200)
        );

        assertEquals("chunking strategy is required", exception.getMessage());
    }
}