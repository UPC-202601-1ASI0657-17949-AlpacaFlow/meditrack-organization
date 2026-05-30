package com.alpacaflow.meditrack.organization.shared.application.internal.outboundservices.acl.rest;

import com.alpacaflow.meditrack.organization.shared.application.internal.outboundservices.acl.ExternalUser;
import com.alpacaflow.meditrack.organization.shared.application.internal.outboundservices.acl.IamContextFacade;
import com.alpacaflow.meditrack.organization.shared.infrastructure.acl.client.RemoteIamUserResponse;
import com.alpacaflow.meditrack.organization.shared.infrastructure.acl.client.RemoteProvisionStaffUserRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Locale;
import java.util.Optional;

/**
 * REST adapter for {@link IamContextFacade}. Calls the IAM microservice to provision and lookup staff users.
 */
@Component
@ConditionalOnProperty(name = "app.iam.adapter", havingValue = "rest")
public class RemoteIamContextFacade implements IamContextFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteIamContextFacade.class);

    private final RestClient restClient;

    public RemoteIamContextFacade(@Value("${app.iam.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl)
                .build();
    }

    @Override
    public Optional<ExternalUser> findUserById(Long userId) {
        if (userId == null || userId <= 0) {
            return Optional.empty();
        }
        try {
            var response = restClient.get()
                    .uri("/api/v1/users/{userId}", userId)
                    .retrieve()
                    .body(RemoteIamUserResponse.class);
            return Optional.ofNullable(response).map(this::toExternalUser);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.error("Failed to find IAM user by id {}: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<ExternalUser> findUserByEmail(String email) {
        var canonical = normalizeEmail(email);
        if (canonical.isEmpty()) {
            return Optional.empty();
        }
        try {
            var response = restClient.get()
                    .uri("/api/v1/users/email/{email}", canonical)
                    .retrieve()
                    .body(RemoteIamUserResponse.class);
            return Optional.ofNullable(response).map(this::toExternalUser);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.error("Failed to find IAM user by email {}: {}", canonical, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Long createMockUser(String email, String role) {
        var canonical = normalizeEmail(email);
        if (canonical.isEmpty()) {
            throw new IllegalArgumentException("email cannot be null or blank");
        }
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("role cannot be null or blank");
        }

        var existing = findUserByEmail(canonical);
        if (existing.isPresent()) {
            return existing.get().id();
        }

        try {
            var response = restClient.post()
                    .uri("/api/v1/users/staff")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new RemoteProvisionStaffUserRequest(canonical, role.toLowerCase(Locale.ROOT)))
                    .retrieve()
                    .body(RemoteIamUserResponse.class);
            if (response == null || response.userId() == null) {
                throw new IllegalStateException("IAM did not return a user id for " + canonical);
            }
            return response.userId();
        } catch (HttpClientErrorException.Conflict | HttpClientErrorException.BadRequest e) {
            return findUserByEmail(canonical)
                    .map(ExternalUser::id)
                    .orElseThrow(() -> new IllegalStateException(
                            "IAM rejected staff provisioning for " + canonical + ": " + e.getMessage()));
        }
    }

    @Override
    public boolean userHasRole(Long userId, String role) {
        if (userId == null || role == null) {
            return false;
        }
        return findUserById(userId)
                .map(user -> user.role().equalsIgnoreCase(role))
                .orElse(false);
    }

    private ExternalUser toExternalUser(RemoteIamUserResponse response) {
        return new ExternalUser(response.userId(), response.email(), response.role());
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
