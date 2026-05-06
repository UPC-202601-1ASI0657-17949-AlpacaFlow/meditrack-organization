package com.alpacafkow.meditrack.organization.organization.interfaces.rest.transform;

import com.alpacafkow.meditrack.organization.organization.domain.model.commands.UpdateOrganizationCommand;
import com.alpacafkow.meditrack.organization.organization.interfaces.rest.resources.request.UpdateOrganizationRequest;

public final class UpdateOrganizationCommandFromRequestAssembler {

    private UpdateOrganizationCommandFromRequestAssembler() {
    }

    public static UpdateOrganizationCommand toCommand(Long organizationId, UpdateOrganizationRequest request) {
        return new UpdateOrganizationCommand(organizationId, request.name(), request.type(), request.email());
    }
}
