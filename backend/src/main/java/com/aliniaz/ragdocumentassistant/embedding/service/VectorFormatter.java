package com.aliniaz.ragdocumentassistant.embedding.service;

import java.util.List;

public interface VectorFormatter {

    String toVector(List<Double> embedding);
}