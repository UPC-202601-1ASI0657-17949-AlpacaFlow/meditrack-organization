package com.alpacaflow.meditrack.organization.admin.interfaces.rest.transform;

import com.alpacaflow.meditrack.organization.admin.domain.model.commands.UpdateAdminCommand;
import com.alpacaflow.meditrack.organization.admin.interfaces.rest.resources.request.UpdateAdminRequest;

public final class UpdateAdminCommandFromRequestAssembler {

    private UpdateAdminCommandFromRequestAssembler() {
    }

    public static UpdateAdminCommand toCommand(Long adminId, UpdateAdminRequest request) {
        return new UpdateAdminCommand(adminId, request.firstName(), request.lastName());
    }
}
