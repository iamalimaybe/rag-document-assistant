package com.aliniaz.ragdocumentassistant.llm.service.impl;

import com.aliniaz.ragdocumentassistant.common.config.OllamaProperties;
import com.aliniaz.ragdocumentassistant.llm.config.LlmProperties;
import com.aliniaz.ragdocumentassistant.llm.service.LlmClient;
import com.aliniaz.ragdocumentassistant.llm.service.LlmGenerationRequest;
import com.aliniaz.ragdocumentassistant.llm.service.LlmGenerationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OllamaLlmClient implements LlmClient {

    private final RestTemplateBuilder restTemplateBuilder;
    private final OllamaProperties ollamaProperties;
    private final LlmProperties llmProperties;

    @Override
    public LlmGenerationResponse generate(LlmGenerationRequest request) {
        if (request.prompt() == null || request.prompt().isBlank()) {
            throw new IllegalArgumentException("Prompt cannot be blank");
        }

        RestTemplate restTemplate = restTemplateBuilder.build();

        OllamaGenerateRequest ollamaRequest = new OllamaGenerateRequest(
                llmProperties.model(),
                request.prompt(),
                false,
                false,
                "json",
                buildOptions()
        );

        try {
            OllamaGenerateResponse response = restTemplate.postForObject(
                    ollamaProperties.baseUrl() + "/api/generate",
                    ollamaRequest,
                    OllamaGenerateResponse.class
            );

            if (response == null) {
                throw new IllegalStateException("Ollama returned an empty response");
            }

            if (response.response() == null || response.response().isBlank()) {
                if (response.thinking() != null && !response.thinking().isBlank()) {
                    throw new IllegalStateException("Ollama returned thinking text but no final response");
                }

                throw new IllegalStateException("Ollama returned no generated text");
            }

            return new LlmGenerationResponse(response.response().trim());
        } catch (RestClientException exception) {
            throw new IllegalStateException("Failed to generate answer through Ollama", exception);
        }
    }

    private Map<String, Object> buildOptions() {
        Map<String, Object> options = new HashMap<>();

        if (llmProperties.temperature() != null) {
            options.put("temperature", llmProperties.temperature());
        }

        if (llmProperties.numPredict() != null) {
            options.put("num_predict", llmProperties.numPredict());
        }

        if (llmProperties.contextWindow() != null) {
            options.put("num_ctx", llmProperties.contextWindow());
        }

        return options;
    }

    private record OllamaGenerateRequest(
            String model,
            String prompt,
            boolean stream,
            Boolean think,
            String format,
            Map<String, Object> options
    ) {
    }

    private record OllamaGenerateResponse(
            String response,
            String thinking
    ) {
    }
}