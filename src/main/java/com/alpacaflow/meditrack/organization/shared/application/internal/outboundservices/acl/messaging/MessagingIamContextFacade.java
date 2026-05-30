package com.alpacaflow.meditrack.organization.shared.application.internal.outboundservices.acl.messaging;

import com.alpacaflow.meditrack.organization.shared.application.internal.outboundservices.acl.ExternalUser;
import com.alpacaflow.meditrack.organization.shared.application.internal.outboundservices.acl.IamContextFacade;
import com.alpacaflow.meditrack.organization.shared.infrastructure.acl.client.RemoteIamUserResponse;
import com.alpacaflow.meditrack.organization.shared.infrastructure.messaging.MessagingQueueNames;
import com.alpacaflow.meditrack.organization.shared.infrastructure.messaging.StaffProvisionRequestMessage;
import com.alpacaflow.meditrack.organization.shared.infrastructure.messaging.StaffProvisionResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Locale;
import java.util.Optional;

/**
 * IAM ACL adapter for the messaging profile: staff provisioning via JMS request-reply,
 * user lookups via REST (synchronous queries).
 */
@Component
@ConditionalOnProperty(name = "app.iam.adapter", havingValue = "jms")
public class MessagingIamContextFacade implements IamContextFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessagingIamContextFacade.class);

    private final RestClient restClient;
    private final JmsTemplate jmsTemplate;

    public MessagingIamContextFacade(
            @Value("${app.iam.base-url}") String baseUrl,
            JmsTemplate jmsTemplate) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl)
                .build();
        this.jmsTemplate = jmsTemplate;
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

        LOGGER.info("Requesting staff provisioning via JMS for email={} role={}", canonical, role);
        var response = jmsTemplate.convertSendAndReceive(
                MessagingQueueNames.STAFF_PROVISION_REQUESTED,
                new StaffProvisionRequestMessage(canonical, role.toLowerCase(Locale.ROOT)));

        if (!(response instanceof StaffProvisionResponseMessage staffResponse)
                || staffResponse.userId() == null) {
            throw new IllegalStateException("IAM did not return a user id for " + canonical);
        }
        return staffResponse.userId();
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
