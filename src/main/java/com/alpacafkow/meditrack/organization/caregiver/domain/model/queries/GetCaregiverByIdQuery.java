package com.alpacafkow.meditrack.organization.caregiver.domain.model.queries;

public record GetCaregiverByIdQuery(Long caregiverId) {
    public GetCaregiverByIdQuery {
        if (caregiverId == null || caregiverId <= 0) {
            throw new IllegalArgumentException("caregiverId cannot be null or less than 1");
        }
    }
}
