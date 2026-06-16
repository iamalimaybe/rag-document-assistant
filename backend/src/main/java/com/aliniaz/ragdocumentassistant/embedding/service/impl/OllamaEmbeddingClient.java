package com.aliniaz.ragdocumentassistant.embedding.service.impl;

import com.aliniaz.ragdocumentassistant.embedding.config.EmbeddingProperties;
import com.aliniaz.ragdocumentassistant.common.config.OllamaProperties;
import com.aliniaz.ragdocumentassistant.embedding.service.EmbeddingClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OllamaEmbeddingClient implements EmbeddingClient {

    private final OllamaProperties ollamaProperties;
    private final EmbeddingProperties embeddingProperties;

    @Override
    public List<Double> embed(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Embedding input cannot be blank");
        }

        RestClient restClient = RestClient.builder()
                .baseUrl(ollamaProperties.baseUrl())
                .build();

        OllamaEmbedResponse response = restClient.post()
                .uri("/api/embed")
                .body(new OllamaEmbedRequest(embeddingProperties.model(), input))
                .retrieve()
                .body(OllamaEmbedResponse.class);

        if (response == null || response.embeddings() == null || response.embeddings().isEmpty()) {
            throw new IllegalStateException("Ollama returned no embeddings");
        }

        List<Double> embedding = response.embeddings().get(0);

        if (embedding.size() != 768) {
            throw new IllegalStateException("Expected embedding dimension 768 but got " + embedding.size());
        }

        return embedding;
    }

    private record OllamaEmbedRequest(
            String model,
            String input
    ) {
    }

    private record OllamaEmbedResponse(
            List<List<Double>> embeddings
    ) {
    }
}