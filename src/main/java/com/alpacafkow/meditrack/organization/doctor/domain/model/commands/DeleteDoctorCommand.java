package com.alpacafkow.meditrack.organization.doctor.domain.model.commands;

public record DeleteDoctorCommand(Long doctorId) {
    public DeleteDoctorCommand {
        if (doctorId == null || doctorId <= 0) {
            throw new IllegalArgumentException("doctorId cannot be null or less than 1");
        }
    }
}
