package com.alpacaflow.meditrack.organization.organization.domain.services;

import com.alpacaflow.meditrack.organization.organization.domain.model.aggregates.Organization;
import com.alpacaflow.meditrack.organization.organization.domain.model.commands.CreateOrganizationCommand;
import com.alpacaflow.meditrack.organization.organization.domain.model.commands.DeleteOrganizationCommand;
import com.alpacaflow.meditrack.organization.organization.domain.model.commands.UpdateOrganizationCommand;

import java.util.Optional;

public interface OrganizationCommandService {
    Long handle(CreateOrganizationCommand command);

    Optional<Organization> handle(UpdateOrganizationCommand command);

    void handle(DeleteOrganizationCommand command);
}
