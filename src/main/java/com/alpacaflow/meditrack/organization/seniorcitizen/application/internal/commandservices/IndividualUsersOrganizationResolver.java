package com.alpacaflow.meditrack.organization.seniorcitizen.application.internal.commandservices;

import com.alpacaflow.meditrack.organization.organization.domain.model.aggregates.Organization;
import com.alpacaflow.meditrack.organization.organization.domain.model.valueobjects.Email;
import com.alpacaflow.meditrack.organization.organization.infrastructure.persistence.jpa.repositories.OrganizationRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resolves senior citizens registered by relatives ({@code organizationId = 0})
 * to a shared placeholder organization in the Organization bounded context.
 */
@Component
public class IndividualUsersOrganizationResolver {

    public static final String ORGANIZATION_NAME = "Individual Users";
    public static final String ORGANIZATION_TYPE = "individual";
    public static final String ORGANIZATION_EMAIL = "individual-users@meditrack.local";

    private final OrganizationRepository organizationRepository;

    public IndividualUsersOrganizationResolver(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @Transactional
    public Organization resolve() {
        return organizationRepository.findByNameIgnoreCase(ORGANIZATION_NAME)
                .orElseGet(this::createIndividualUsersOrganization);
    }

    private Organization createIndividualUsersOrganization() {
        var organization = new Organization(
                ORGANIZATION_NAME,
                ORGANIZATION_TYPE,
                new Email(ORGANIZATION_EMAIL));
        var saved = organizationRepository.save(organization);
        saved.publishCreatedEvent();
        return organizationRepository.save(saved);
    }
}
