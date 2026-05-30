package com.alpacaflow.meditrack.organization.shared.infrastructure.messaging;

import com.alpacaflow.meditrack.organization.admin.domain.exceptions.AdminAlreadyExistsForUserException;
import com.alpacaflow.meditrack.organization.admin.domain.model.commands.CreateAdminCommand;
import com.alpacaflow.meditrack.organization.admin.domain.services.AdminCommandService;
import com.alpacaflow.meditrack.organization.organization.domain.exceptions.OrganizationDuplicateEmailException;
import com.alpacaflow.meditrack.organization.organization.domain.exceptions.OrganizationDuplicateNameException;
import com.alpacaflow.meditrack.organization.organization.domain.model.commands.CreateOrganizationCommand;
import com.alpacaflow.meditrack.organization.organization.domain.services.OrganizationCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "app.messaging.enabled", havingValue = "true")
public class AdminRegistrationMessageHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminRegistrationMessageHandler.class);

    private final OrganizationCommandService organizationCommandService;
    private final AdminCommandService adminCommandService;

    public AdminRegistrationMessageHandler(
            OrganizationCommandService organizationCommandService,
            AdminCommandService adminCommandService) {
        this.organizationCommandService = organizationCommandService;
        this.adminCommandService = adminCommandService;
    }

    @Transactional
    public void handle(AdminRegistrationRequestedMessage message) {
        LOGGER.info("Processing admin registration message eventId={} userId={}",
                message.eventId(), message.userId());

        try {
            var organizationId = organizationCommandService.handle(new CreateOrganizationCommand(
                    message.organizationName(),
                    message.organizationType(),
                    message.email()));

            adminCommandService.handle(new CreateAdminCommand(
                    organizationId,
                    message.userId(),
                    message.firstName(),
                    message.lastName()));

            LOGGER.info("Admin registration completed for userId={} organizationId={}",
                    message.userId(), organizationId);
        } catch (OrganizationDuplicateNameException | OrganizationDuplicateEmailException e) {
            LOGGER.warn("Organization already exists for admin registration userId={}: {}",
                    message.userId(), e.getMessage());
        } catch (AdminAlreadyExistsForUserException e) {
            LOGGER.warn("Admin already exists for userId={}: {}", message.userId(), e.getMessage());
        }
    }
}
