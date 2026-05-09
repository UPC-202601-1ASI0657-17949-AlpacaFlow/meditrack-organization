package com.alpacafkow.meditrack.organization.admin.interfaces.rest.transform;

import com.alpacafkow.meditrack.organization.admin.domain.model.commands.CreateAdminCommand;
import com.alpacafkow.meditrack.organization.admin.interfaces.rest.resources.request.CreateAdminRequest;

public final class CreateAdminCommandFromRequestAssembler {

    private CreateAdminCommandFromRequestAssembler() {
    }

    public static CreateAdminCommand toCommand(CreateAdminRequest request) {
        return new CreateAdminCommand(
                request.organizationId(),
                request.userId(),
                request.firstName(),
                request.lastName()
        );
    }
}
