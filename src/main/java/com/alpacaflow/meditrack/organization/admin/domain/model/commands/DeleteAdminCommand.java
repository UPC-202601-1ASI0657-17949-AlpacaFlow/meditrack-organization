package com.alpacaflow.meditrack.organization.admin.domain.model.commands;

public record DeleteAdminCommand(Long adminId) {
    public DeleteAdminCommand {
        if (adminId == null || adminId <= 0) {
            throw new IllegalArgumentException("adminId cannot be null or less than 1");
        }
    }
}
