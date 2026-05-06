package com.alpacafkow.meditrack.organization.organization.interfaces.rest.transform;

import com.alpacafkow.meditrack.organization.organization.domain.model.commands.CreateOrganizationCommand;
import com.alpacafkow.meditrack.organization.organization.interfaces.rest.resources.request.CreateOrganizationRequest;

public final class CreateOrganizationCommandFromRequestAssembler {

    private CreateOrganizationCommandFromRequestAssembler() {
    }

    public static CreateOrganizationCommand toCommand(CreateOrganizationRequest request) {
        return new CreateOrganizationCommand(request.name(), request.type(), request.email());
    }
}
