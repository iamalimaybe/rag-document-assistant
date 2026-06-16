package com.aliniaz.ragdocumentassistant.rag.api;

import com.aliniaz.ragdocumentassistant.rag.api.request.AskDocumentRequest;
import com.aliniaz.ragdocumentassistant.rag.api.response.AskDocumentResponse;
import com.aliniaz.ragdocumentassistant.rag.service.AskDocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AskDocumentController {

    private final AskDocumentService askDocumentService;

    @PostMapping("/api/documents/{documentId}/ask")
    public AskDocumentResponse ask(
            @PathVariable Long documentId,
            @Valid @RequestBody AskDocumentRequest request
    ) {
        return askDocumentService.ask(documentId, request);
    }
}