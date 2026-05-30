package com.alpacaflow.meditrack.organization.shared.infrastructure.messaging;

/**
 * Request-reply command sent to IAM to provision a doctor/caregiver user.
 */
public record StaffProvisionRequestMessage(
        String email,
        String role
) {
}
