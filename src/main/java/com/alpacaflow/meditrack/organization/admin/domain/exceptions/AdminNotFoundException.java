package com.alpacaflow.meditrack.organization.admin.domain.exceptions;

public class AdminNotFoundException extends RuntimeException {
    public AdminNotFoundException(Long adminId) {
        super("Admin with ID " + adminId + " not found.");
    }
}
