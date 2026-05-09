package com.alpacafkow.meditrack.organization.admin.domain.model.queries;

public record GetAllAdminsByOrganizationIdQuery(Long organizationId) {
    public GetAllAdminsByOrganizationIdQuery {
        if (organizationId == null || organizationId <= 0) {
            throw new IllegalArgumentException("organizationId cannot be null or less than 1");
        }
    }
}
