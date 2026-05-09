package com.alpacafkow.meditrack.organization.admin.domain.model.queries;

public record GetAdminByIdQuery(Long adminId) {
    public GetAdminByIdQuery {
        if (adminId == null || adminId <= 0) {
            throw new IllegalArgumentException("adminId cannot be null or less than 1");
        }
    }
}
