package com.alpacafkow.meditrack.organization.shared.application.internal.outboundservices.acl;

/**
 * Internal representation of a user owned by the IAM bounded context.
 * Used by the {@link IamContextFacade} so the rest of the organization microservice
 * never depends on IAM domain types directly (Anti-Corruption Layer).
 */
public record ExternalUser(Long id, String email, String role) {
}
