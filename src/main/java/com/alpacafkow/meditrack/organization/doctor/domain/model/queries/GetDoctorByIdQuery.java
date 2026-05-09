package com.alpacafkow.meditrack.organization.doctor.domain.model.queries;

public record GetDoctorByIdQuery(Long doctorId) {
    public GetDoctorByIdQuery {
        if (doctorId == null || doctorId <= 0) {
            throw new IllegalArgumentException("doctorId cannot be null or less than 1");
        }
    }
}
