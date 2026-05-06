package com.alpacafkow.meditrack.organization.organization.domain.model.commands;

public record CreateOrganizationCommand(
        String name,
        String type,
        String email
) {
    public CreateOrganizationCommand {
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
