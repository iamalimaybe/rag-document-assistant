package com.aliniaz.ragdocumentassistant.document.service;

import java.util.List;

public interface DocumentChunker {

    List<DocumentChunkData> chunk(String text);
}