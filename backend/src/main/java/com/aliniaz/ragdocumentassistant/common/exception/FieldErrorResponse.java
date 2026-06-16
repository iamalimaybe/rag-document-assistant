package com.aliniaz.ragdocumentassistant.common.exception;

public record FieldErrorResponse(
        String field,
        String message
) {
}