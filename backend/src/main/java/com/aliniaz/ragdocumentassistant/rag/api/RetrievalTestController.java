package com.aliniaz.ragdocumentassistant.rag.api;

import com.aliniaz.ragdocumentassistant.rag.api.request.RetrievalTestRequest;
import com.aliniaz.ragdocumentassistant.rag.api.response.RetrievalTestResponse;
import com.aliniaz.ragdocumentassistant.rag.service.RetrievalTestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RetrievalTestController {

    private final RetrievalTestService retrievalTestService;

    @PostMapping("/api/documents/{documentId}/retrieval-test")
    public RetrievalTestResponse retrieve(
            @PathVariable Long documentId,
            @Valid @RequestBody RetrievalTestRequest request
    ) {
        return retrievalTestService.retrieve(documentId, request);
    }
}