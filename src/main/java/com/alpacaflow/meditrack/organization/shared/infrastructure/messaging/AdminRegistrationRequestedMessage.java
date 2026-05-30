package com.alpacaflow.meditrack.organization.shared.infrastructure.messaging;

/**
 * Async command consumed from IAM after an admin user signs up.
 */
public record AdminRegistrationRequestedMessage(
        String eventId,
        Long userId,
        String email,
        String firstName,
        String lastName,
        String organizationName,
        String organizationType
) {
}
