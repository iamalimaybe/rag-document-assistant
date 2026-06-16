package com.aliniaz.ragdocumentassistant.document.service.impl;

import com.aliniaz.ragdocumentassistant.document.config.ChunkingProperties;
import com.aliniaz.ragdocumentassistant.document.config.DocumentChunkingStrategy;
import com.aliniaz.ragdocumentassistant.document.service.DocumentChunkData;
import com.aliniaz.ragdocumentassistant.document.service.DocumentChunker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfigurableDocumentChunker implements DocumentChunker {

    private final ChunkingProperties chunkingProperties;
    private final DeterministicDocumentChunker deterministicDocumentChunker;
    private final StructureAwareDocumentChunker structureAwareDocumentChunker;

    @Override
    public List<DocumentChunkData> chunk(String text) {
        return switch (chunkingProperties.strategy()) {
            case DETERMINISTIC -> deterministicDocumentChunker.chunk(text);
            case STRUCTURE_AWARE -> structureAwareDocumentChunker.chunk(text);
        };
    }
}