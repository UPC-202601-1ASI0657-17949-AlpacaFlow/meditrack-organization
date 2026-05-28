package com.alpacaflow.meditrack.organization.organization.application.internal.commandservices;

import com.alpacaflow.meditrack.organization.organization.domain.exceptions.OrganizationDuplicateEmailException;
import com.alpacaflow.meditrack.organization.organization.domain.exceptions.OrganizationDuplicateNameException;
import com.alpacaflow.meditrack.organization.organization.domain.exceptions.OrganizationNotFoundException;
import com.alpacaflow.meditrack.organization.organization.domain.model.aggregates.Organization;
import com.alpacaflow.meditrack.organization.organization.domain.model.commands.CreateOrganizationCommand;
import com.alpacaflow.meditrack.organization.organization.domain.model.commands.DeleteOrganizationCommand;
import com.alpacaflow.meditrack.organization.organization.domain.model.commands.UpdateOrganizationCommand;
import com.alpacaflow.meditrack.organization.organization.domain.model.valueobjects.Email;
import com.alpacaflow.meditrack.organization.organization.domain.services.OrganizationCommandService;
import com.alpacaflow.meditrack.organization.organization.infrastructure.persistence.jpa.repositories.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class OrganizationCommandServiceImpl implements OrganizationCommandService {

    private final OrganizationRepository organizationRepository;

    public OrganizationCommandServiceImpl(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @Override
    @Transactional
    public Long handle(CreateOrganizationCommand command) {
        var name = command.name().trim();
        var type = command.type().trim();
        var email = new Email(command.email().trim());

        if (organizationRepository.existsByNameIgnoreCase(name)) {
            throw new OrganizationDuplicateNameException("An organization with this name is already registered");
        }
        if (organizationRepository.existsByEmail_Value(email.value())) {
            throw new OrganizationDuplicateEmailException("An organization with this email is already registered");
        }

        var organization = new Organization(name, type, email);
        var saved = organizationRepository.save(organization);
        saved.publishCreatedEvent();
        organizationRepository.save(saved);
        return saved.getId();
    }

    @Override
    @Transactional
    public Optional<Organization> handle(UpdateOrganizationCommand command) {
        var organization = organizationRepository.findById(command.organizationId())
                .orElseThrow(() -> new OrganizationNotFoundException(command.organizationId()));

        var name = command.name().trim();
        var email = new Email(command.email().trim());

        if (organizationRepository.existsByNameIgnoreCaseAndIdNot(name, command.organizationId())) {
            throw new OrganizationDuplicateNameException("An organization with this name is already registered");
        }
        if (organizationRepository.existsByEmail_ValueAndIdNot(email.value(), command.organizationId())) {
            throw new OrganizationDuplicateEmailException("An organization with this email is already registered");
        }

        organization.updateInformation(name, command.type().trim(), email);
        return Optional.of(organizationRepository.save(organization));
    }

    @Override
    @Transactional
    public void handle(DeleteOrganizationCommand command) {
        if (!organizationRepository.existsById(command.organizationId())) {
            throw new OrganizationNotFoundException(command.organizationId());
        }
        organizationRepository.deleteById(command.organizationId());
    }
}
