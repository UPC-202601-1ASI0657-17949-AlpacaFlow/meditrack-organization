package com.alpacaflow.meditrack.organization.shared.application.internal.outboundservices.acl;

import java.util.Optional;

/**
 * Anti-Corruption Layer port for the IAM bounded context.
 * <p>
 * The organization microservice depends only on this interface, never on IAM internals.
 * Two adapters can be wired through {@code app.iam.adapter}:
 * <ul>
 *     <li><b>stub</b> (default, dev/test): in-memory implementation that lets the service
 *     run and be tested without IAM running.</li>
 *     <li><b>rest</b> (future): HTTP client against the real IAM microservice once it
 *     exposes its REST API.</li>
 * </ul>
 * Replacing the stub by the REST adapter requires no changes in domain or application services.
 */
public interface IamContextFacade {

    /**
     * Looks up a user by its unique identifier in IAM.
     */
    Optional<ExternalUser> findUserById(Long userId);

    /**
     * Looks up a user by email. Used when an aggregate (Doctor / Caregiver) needs to
     * autoprovision a user account if none exists for the given email.
     */
    Optional<ExternalUser> findUserByEmail(String email);

    /**
     * Creates a mock user with the given email and role and returns its identifier.
     * Mirrors the behavior used by the monolith for staff autoprovisioning.
     */
    Long createMockUser(String email, String role);

    /**
     * Returns true if the user has the expected role.
     * Implementations may be permissive when role information is unknown (stub).
     */
    boolean userHasRole(Long userId, String role);
}
