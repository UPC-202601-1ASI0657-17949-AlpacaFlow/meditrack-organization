package com.alpacaflow.meditrack.organization.shared.infrastructure.acl.client;

public record RemoteProvisionStaffUserRequest(
        String email,
        String role
) {
}
