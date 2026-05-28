package com.alpacaflow.meditrack.organization.admin.domain.exceptions;

public class AdminInvalidRoleException extends RuntimeException {
    public AdminInvalidRoleException(Long userId) {
        super("User with ID %d does not have role 'admin' in IAM.".formatted(userId));
    }
}
