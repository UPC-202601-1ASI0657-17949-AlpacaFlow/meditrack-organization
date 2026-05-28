package com.alpacaflow.meditrack.organization.doctor.domain.model.queries;

public record GetAllDoctorsByOrganizationIdQuery(Long organizationId) {
    public GetAllDoctorsByOrganizationIdQuery {
        if (organizationId == null || organizationId <= 0) {
            throw new IllegalArgumentException("organizationId cannot be null or less than 1");
        }
    }
}
