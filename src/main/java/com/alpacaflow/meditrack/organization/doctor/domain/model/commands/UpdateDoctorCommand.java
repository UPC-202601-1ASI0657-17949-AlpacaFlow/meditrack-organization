package com.alpacaflow.meditrack.organization.doctor.domain.model.commands;

public record UpdateDoctorCommand(
        Long doctorId,
        String firstName,
        String lastName,
        String specialty,
        Integer age,
        String email,
        String phoneNumber,
        String imageUrl
) {
    public UpdateDoctorCommand {
        if (doctorId == null || doctorId <= 0) {
            throw new IllegalArgumentException("doctorId cannot be null or less than 1");
        }
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("firstName cannot be null or blank");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("lastName cannot be null or blank");
        }
        if (specialty == null || specialty.isBlank()) {
            throw new IllegalArgumentException("specialty cannot be null or blank");
        }
        if (age == null || age <= 0) {
            throw new IllegalArgumentException("age cannot be null or less than 1");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email cannot be null or blank");
        }
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("phoneNumber cannot be null or blank");
        }
    }
}
