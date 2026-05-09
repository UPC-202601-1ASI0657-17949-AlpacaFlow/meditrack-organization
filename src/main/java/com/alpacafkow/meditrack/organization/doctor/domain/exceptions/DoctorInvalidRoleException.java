package com.alpacafkow.meditrack.organization.doctor.domain.exceptions;

public class DoctorInvalidRoleException extends RuntimeException {
    public DoctorInvalidRoleException(Long userId, String currentRole) {
        super("User with ID %d does not have role 'doctor' in IAM. Current role: %s".formatted(userId, currentRole));
    }
}
