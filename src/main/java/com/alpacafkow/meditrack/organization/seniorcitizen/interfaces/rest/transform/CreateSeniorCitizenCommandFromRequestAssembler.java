package com.alpacafkow.meditrack.organization.seniorcitizen.interfaces.rest.transform;

import com.alpacafkow.meditrack.organization.seniorcitizen.domain.model.commands.CreateSeniorCitizenCommand;
import com.alpacafkow.meditrack.organization.seniorcitizen.interfaces.rest.resources.request.CreateSeniorCitizenRequest;

public final class CreateSeniorCitizenCommandFromRequestAssembler {

    private CreateSeniorCitizenCommandFromRequestAssembler() {
    }

    public static CreateSeniorCitizenCommand toCommand(CreateSeniorCitizenRequest request) {
        return new CreateSeniorCitizenCommand(
                request.organizationId(),
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
