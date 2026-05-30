package com.alpacaflow.meditrack.organization.shared.infrastructure.acl.client;

public record RemoteIamUserResponse(
        Long userId,
        String email,
        String role
) {
}
