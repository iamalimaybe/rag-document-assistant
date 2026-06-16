package com.aliniaz.ragdocumentassistant.embedding.service.impl;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PgVectorFormatterTest {

    private final PgVectorFormatter formatter = new PgVectorFormatter();

    @Test
    void toVectorFormatsEmbeddingAsPgVectorString() {
        String vector = formatter.toVector(List.of(1.0, 2.0, 3.0));

        assertEquals("[1.0,2.0,3.0]", vector);
    }

    @Test
    void toVectorKeepsDecimalValuesStable() {
        String vector = formatter.toVector(List.of(0.123456, -1.5, 42.0));

        assertEquals("[0.123456,-1.5,42.0]", vector);
    }

    @Test
    void toVectorRejectsNullVector() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> formatter.toVector(null)
        );

        assertEquals("Embedding vector cannot be empty", exception.getMessage());
    }

    @Test
    void toVectorRejectsEmptyVector() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> formatter.toVector(List.of())
        );

        assertEquals("Embedding vector cannot be empty", exception.getMessage());
    }

    @Test
    void toVectorRejectsNullValuesInsideVector() {
        List<Double> embedding = new java.util.ArrayList<>();
        embedding.add(1.0);
        embedding.add(null);
        embedding.add(3.0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> formatter.toVector(embedding)
        );

        assertEquals("Embedding vector cannot contain null values", exception.getMessage());
    }
}