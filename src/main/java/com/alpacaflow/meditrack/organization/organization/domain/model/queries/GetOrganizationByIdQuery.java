package com.alpacaflow.meditrack.organization.organization.domain.model.queries;

public record GetOrganizationByIdQuery(Long organizationId) {
    public GetOrganizationByIdQuery {
        if (organizationId == null || organizationId <= 0) {
            throw new IllegalArgumentException("organizationId cannot be null or less than 1");
        }
    }
}
