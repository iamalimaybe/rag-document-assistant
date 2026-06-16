package com.aliniaz.ragdocumentassistant.rag.service;

import com.aliniaz.ragdocumentassistant.rag.api.request.AskDocumentRequest;
import com.aliniaz.ragdocumentassistant.rag.api.response.AskDocumentResponse;

public interface AskDocumentService {

    AskDocumentResponse ask(Long documentId, AskDocumentRequest request);
}