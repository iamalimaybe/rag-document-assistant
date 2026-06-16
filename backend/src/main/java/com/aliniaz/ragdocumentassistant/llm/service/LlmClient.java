package com.aliniaz.ragdocumentassistant.llm.service;

public interface LlmClient {

    LlmGenerationResponse generate(LlmGenerationRequest request);
}