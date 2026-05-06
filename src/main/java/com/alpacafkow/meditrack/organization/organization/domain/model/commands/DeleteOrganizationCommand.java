package com.alpacafkow.meditrack.organization.organization.domain.model.commands;

public record DeleteOrganizationCommand(Long organizationId) {
    public DeleteOrganizationCommand {
        if (organizationId == null || organizationId <= 0) {
            throw new IllegalArgumentException("organizationId cannot be null or less than 1");
        }
    }
}
