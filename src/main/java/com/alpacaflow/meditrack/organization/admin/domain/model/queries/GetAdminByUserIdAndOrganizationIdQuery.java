package com.alpacaflow.meditrack.organization.admin.domain.model.queries;

public record GetAdminByUserIdAndOrganizationIdQuery(Long userId, Long organizationId) {
    public GetAdminByUserIdAndOrganizationIdQuery {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId cannot be null or less than 1");
        }
        if (organizationId == null || organizationId <= 0) {
            throw new IllegalArgumentException("organizationId cannot be null or less than 1");
        }
    }
}
