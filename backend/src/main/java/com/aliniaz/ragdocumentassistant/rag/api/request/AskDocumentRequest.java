package com.aliniaz.ragdocumentassistant.rag.api.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AskDocumentRequest(
        @NotBlank(message = "Question is required")
        String question,

        @Min(value = 1, message = "topK must be at least 1")
        @Max(value = 20, message = "topK cannot be greater than 20")
        Integer topK
) {
}