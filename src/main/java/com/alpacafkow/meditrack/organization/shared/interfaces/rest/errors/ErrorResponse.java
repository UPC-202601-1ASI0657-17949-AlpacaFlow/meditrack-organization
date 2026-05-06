package com.alpacafkow.meditrack.organization.shared.interfaces.rest.errors;

import java.time.OffsetDateTime;

public record ErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String code,
        String message,
        String path
) {
    public static ErrorResponse of(int status, String code, String message, String path) {
        return new ErrorResponse(OffsetDateTime.now(), status, code, message, path);
    }
}
