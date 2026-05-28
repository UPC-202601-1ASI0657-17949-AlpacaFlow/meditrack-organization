package com.alpacaflow.meditrack.organization.caregiver.domain.exceptions;

public class CaregiverNotFoundException extends RuntimeException {
    public CaregiverNotFoundException(Long caregiverId) {
        super("Caregiver with ID " + caregiverId + " not found.");
    }
}
