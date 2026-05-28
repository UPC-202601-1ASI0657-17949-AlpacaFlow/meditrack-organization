package com.alpacaflow.meditrack.organization.caregiver.interfaces.rest.transform;

import com.alpacaflow.meditrack.organization.caregiver.domain.model.commands.CreateCaregiverCommand;
import com.alpacaflow.meditrack.organization.caregiver.interfaces.rest.resources.request.CreateCaregiverRequest;

public final class CreateCaregiverCommandFromRequestAssembler {

    private CreateCaregiverCommandFromRequestAssembler() {
    }

    public static CreateCaregiverCommand toCommand(CreateCaregiverRequest request) {
        return new CreateCaregiverCommand(
                request.organizationId(),
                request.userId(),
                request.firstName(),
                request.lastName(),
                request.age(),
                request.email(),
                request.phoneNumber(),
                request.imageUrl()
        );
    }
}
