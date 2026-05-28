package com.alpacaflow.meditrack.organization.seniorcitizen.interfaces.rest.transform;

import com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.commands.UpdateSeniorCitizenCommand;
import com.alpacaflow.meditrack.organization.seniorcitizen.interfaces.rest.resources.request.UpdateSeniorCitizenRequest;

public final class UpdateSeniorCitizenCommandFromRequestAssembler {

    private UpdateSeniorCitizenCommandFromRequestAssembler() {
    }

    public static UpdateSeniorCitizenCommand toCommand(Long seniorCitizenId, UpdateSeniorCitizenRequest request) {
        return new UpdateSeniorCitizenCommand(
                seniorCitizenId,
                request.firstName(),
                request.lastName(),
                request.birthDate(),
                request.gender(),
                request.weight(),
                request.dni(),
                request.height(),
                request.imageUrl(),
                request.deviceId()
        );
    }
}
