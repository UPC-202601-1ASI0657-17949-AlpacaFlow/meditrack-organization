package com.alpacaflow.meditrack.organization.organization.domain.exceptions;

public class OrganizationNotFoundException extends RuntimeException {
    public OrganizationNotFoundException(Long organizationId) {
        super("Organization with ID " + organizationId + " not found.");
    }
}
