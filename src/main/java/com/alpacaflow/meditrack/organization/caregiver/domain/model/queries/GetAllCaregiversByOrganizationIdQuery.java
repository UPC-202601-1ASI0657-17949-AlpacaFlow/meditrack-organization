package com.alpacaflow.meditrack.organization.caregiver.domain.model.queries;

public record GetAllCaregiversByOrganizationIdQuery(Long organizationId) {
    public GetAllCaregiversByOrganizationIdQuery {
        if (organizationId == null || organizationId <= 0) {
            throw new IllegalArgumentException("organizationId cannot be null or less than 1");
        }
    }
}
