package com.alpacaflow.meditrack.organization.shared.application.internal.outboundservices.acl.stub;

import com.alpacaflow.meditrack.organization.shared.application.internal.outboundservices.acl.ExternalUser;
import com.alpacaflow.meditrack.organization.shared.application.internal.outboundservices.acl.IamContextFacade;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory stub adapter for {@link IamContextFacade}.
 * <p>
 * Active by default and explicitly when {@code app.iam.adapter=stub}. Lets the organization
 * microservice run, expose Swagger and pass unit / functional tests without requiring the
 * IAM microservice to be running.
 * <p>
 * Behaviour:
 * <ul>
 *     <li>{@link #findUserById(Long)}: returns a synthesized {@link ExternalUser} for any
 *     positive id (with role {@code unknown}). Persists it so subsequent role assignments
 *     are honored.</li>
 *     <li>{@link #findUserByEmail(String)}: returns a previously created user with that email.</li>
 *     <li>{@link #createMockUser(String, String)}: persists a new user with the given role
 *     and returns its generated id.</li>
 *     <li>{@link #userHasRole(Long, String)}: permissive when role is {@code unknown}, strict
 *     once a role has been explicitly assigned.</li>
 * </ul>
 */
@Component
@ConditionalOnProperty(name = "app.iam.adapter", havingValue = "stub", matchIfMissing = true)
public class LocalStubIamContextFacade implements IamContextFacade {

    private static final String UNKNOWN_ROLE = "unknown";

    private final ConcurrentHashMap<Long, ExternalUser> usersById = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1000);

    @Override
    public Optional<ExternalUser> findUserById(Long userId) {
        if (userId == null || userId <= 0) {
            return Optional.empty();
        }
        return Optional.of(usersById.computeIfAbsent(userId, this::synthesize));
    }

    @Override
    public Optional<ExternalUser> findUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        var canonical = email.trim().toLowerCase();
        return usersById.values().stream()
                .filter(user -> user.email().equalsIgnoreCase(canonical))
                .findFirst();
    }

    @Override
    public Long createMockUser(String email, String role) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email cannot be null or blank");
        }
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("role cannot be null or blank");
        }
        var existing = findUserByEmail(email);
        if (existing.isPresent()) {
            return existing.get().id();
        }
        var id = sequence.incrementAndGet();
        usersById.put(id, new ExternalUser(id, email.trim().toLowerCase(), role.toLowerCase()));
        return id;
    }

    @Override
    public boolean userHasRole(Long userId, String role) {
        if (userId == null || role == null) {
            return false;
        }
        return findUserById(userId)
                .map(user -> UNKNOWN_ROLE.equalsIgnoreCase(user.role())
                        || user.role().equalsIgnoreCase(role))
                .orElse(false);
    }

    private ExternalUser synthesize(Long id) {
        return new ExternalUser(id, "user-" + id + "@stub.local", UNKNOWN_ROLE);
    }
}
