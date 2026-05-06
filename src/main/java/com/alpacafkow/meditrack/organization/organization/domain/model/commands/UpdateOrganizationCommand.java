package com.alpacafkow.meditrack.organization.organization.domain.model.commands;

public record UpdateOrganizationCommand(
        Long organizationId,
        String name,
        String type,
        String email
) {
    public UpdateOrganizationCommand {
        if (organizationId == null || organizationId <= 0) {
            throw new IllegalArgumentException("organizationId cannot be null or less than 1");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Type cannot be null or blank");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
    }
}
