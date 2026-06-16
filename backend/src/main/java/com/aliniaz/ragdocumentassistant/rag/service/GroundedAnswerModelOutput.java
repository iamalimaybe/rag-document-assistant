package com.aliniaz.ragdocumentassistant.rag.service;

import com.aliniaz.ragdocumentassistant.rag.domain.AnswerStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GroundedAnswerModelOutput(
        @JsonProperty("answer_status")
        AnswerStatus answerStatus,

        String answer,

        List<Long> citations
) {
}