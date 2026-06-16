package com.aliniaz.ragdocumentassistant.rag.service;

import com.aliniaz.ragdocumentassistant.rag.api.request.RetrievalTestRequest;
import com.aliniaz.ragdocumentassistant.rag.api.response.RetrievalTestResponse;

public interface RetrievalTestService {

    RetrievalTestResponse retrieve(Long documentId, RetrievalTestRequest request);
}