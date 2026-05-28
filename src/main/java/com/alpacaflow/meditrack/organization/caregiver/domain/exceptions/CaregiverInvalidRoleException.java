package com.alpacaflow.meditrack.organization.caregiver.domain.exceptions;

public class CaregiverInvalidRoleException extends RuntimeException {
    public CaregiverInvalidRoleException(Long userId, String expectedRole) {
        super("User with ID " + userId + " does not have the required role '" + expectedRole + "'.");
    }
}
