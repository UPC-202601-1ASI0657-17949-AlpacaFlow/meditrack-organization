package com.alpacaflow.meditrack.organization.shared.infrastructure.security;

import com.alpacaflow.meditrack.organization.shared.interfaces.rest.errors.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Returns 401 + {@link ErrorResponse} JSON when an unauthenticated client hits a protected endpoint.
 * Invoked when a client hits a protected route without a valid Bearer JWT.
 */
@Component
public class UnauthorizedRequestHandlerEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public UnauthorizedRequestHandlerEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String message = authException.getMessage() != null ? authException.getMessage() : "Authentication required";
        var body = ErrorResponse.of(401, "UNAUTHORIZED", message, request.getRequestURI());
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
