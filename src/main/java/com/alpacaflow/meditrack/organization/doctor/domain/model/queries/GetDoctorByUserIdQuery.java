package com.alpacaflow.meditrack.organization.doctor.domain.model.queries;

public record GetDoctorByUserIdQuery(Long userId) {
    public GetDoctorByUserIdQuery {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId cannot be null or less than 1");
        }
    }
}
