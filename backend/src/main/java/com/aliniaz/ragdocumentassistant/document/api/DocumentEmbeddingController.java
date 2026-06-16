package com.aliniaz.ragdocumentassistant.document.api;

import com.aliniaz.ragdocumentassistant.document.service.DocumentEmbeddingService;
import com.aliniaz.ragdocumentassistant.document.service.EmbeddedDocumentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DocumentEmbeddingController {

    private final DocumentEmbeddingService documentEmbeddingService;

    @PostMapping("/api/documents/{documentId}/embeddings")
    public EmbeddedDocumentResponse embedDocumentChunks(@PathVariable Long documentId) {
        return documentEmbeddingService.embedDocumentChunks(documentId);
    }
}