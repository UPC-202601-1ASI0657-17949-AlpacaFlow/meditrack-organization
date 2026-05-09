package com.alpacafkow.meditrack.organization.admin.interfaces.rest.transform;

import com.alpacafkow.meditrack.organization.admin.domain.model.aggregates.Admin;
import com.alpacafkow.meditrack.organization.admin.interfaces.rest.resources.response.AdminResponse;

public final class AdminResponseFromEntityAssembler {

    private AdminResponseFromEntityAssembler() {
    }

    public static AdminResponse toResponse(Admin admin) {
        return new AdminResponse(
                admin.getId(),
                admin.getOrganizationId(),
                admin.getUserId(),
                admin.getFirstName(),
                admin.getLastName(),
                admin.getFullName(),
                admin.getCreatedAt(),
                admin.getUpdatedAt()
        );
    }
}
