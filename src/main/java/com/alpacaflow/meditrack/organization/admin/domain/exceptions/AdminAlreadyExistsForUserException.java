package com.alpacaflow.meditrack.organization.admin.domain.exceptions;

public class AdminAlreadyExistsForUserException extends RuntimeException {
    public AdminAlreadyExistsForUserException(Long userId, Long organizationId) {
        super("Admin already exists for userId %d in organization %d.".formatted(userId, organizationId));
    }
}
