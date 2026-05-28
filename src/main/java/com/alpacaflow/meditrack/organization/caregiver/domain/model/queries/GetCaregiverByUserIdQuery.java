package com.alpacaflow.meditrack.organization.caregiver.domain.model.queries;

public record GetCaregiverByUserIdQuery(Long userId) {
    public GetCaregiverByUserIdQuery {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId cannot be null or less than 1");
        }
    }
}
