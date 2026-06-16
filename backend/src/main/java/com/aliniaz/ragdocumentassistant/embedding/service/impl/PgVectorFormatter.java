package com.aliniaz.ragdocumentassistant.embedding.service.impl;

import com.aliniaz.ragdocumentassistant.embedding.service.VectorFormatter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PgVectorFormatter implements VectorFormatter {

    @Override
    public String toVector(List<Double> embedding) {
        if (embedding == null || embedding.isEmpty()) {
            throw new IllegalArgumentException("Embedding vector cannot be empty");
        }

        if (embedding.stream().anyMatch(value -> value == null)) {
            throw new IllegalArgumentException("Embedding vector cannot contain null values");
        }

        return embedding.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",", "[", "]"));
    }
}