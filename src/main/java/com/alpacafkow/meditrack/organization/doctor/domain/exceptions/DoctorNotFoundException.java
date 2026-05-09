package com.alpacafkow.meditrack.organization.doctor.domain.exceptions;

public class DoctorNotFoundException extends RuntimeException {
    public DoctorNotFoundException(Long doctorId) {
        super("Doctor with ID " + doctorId + " not found.");
    }
}
