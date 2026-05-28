package com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.queries;

public record GetSeniorCitizensByAssignedDoctorIdQuery(Long doctorId) {
    public GetSeniorCitizensByAssignedDoctorIdQuery {
        if (doctorId == null || doctorId <= 0) {
            throw new IllegalArgumentException("doctorId cannot be null or less than 1");
        }
    }
}
