package com.aliniaz.ragdocumentassistant.embedding.service;

import java.util.List;

public interface EmbeddingClient {

    List<Double> embed(String input);
}