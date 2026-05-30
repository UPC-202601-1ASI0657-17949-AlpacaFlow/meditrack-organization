package com.alpacaflow.meditrack.organization.shared.infrastructure.messaging;

/**
 * Reply from IAM with the provisioned staff user id.
 */
public record StaffProvisionResponseMessage(
        Long userId,
        String email,
        String role
) {
}
