package com.alpacafkow.meditrack.organization.seniorcitizen.domain.model.queries;

public record GetSeniorCitizensByAssignedCaregiverIdQuery(Long caregiverId) {
    public GetSeniorCitizensByAssignedCaregiverIdQuery {
        if (caregiverId == null || caregiverId <= 0) {
            throw new IllegalArgumentException("caregiverId cannot be null or less than 1");
        }
    }
}
