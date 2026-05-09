package com.alpacafkow.meditrack.organization.seniorcitizen.domain.model.queries;

public record GetAllSeniorCitizensByOrganizationIdQuery(Long organizationId) {
    public GetAllSeniorCitizensByOrganizationIdQuery {
        if (organizationId == null || organizationId <= 0) {
            throw new IllegalArgumentException("organizationId cannot be null or less than 1");
        }
    }
}
