package com.alpacafkow.meditrack.organization.caregiver.interfaces.rest.transform;

import com.alpacafkow.meditrack.organization.caregiver.domain.model.commands.UpdateCaregiverCommand;
import com.alpacafkow.meditrack.organization.caregiver.interfaces.rest.resources.request.UpdateCaregiverRequest;

public final class UpdateCaregiverCommandFromRequestAssembler {

    private UpdateCaregiverCommandFromRequestAssembler() {
    }

    public static UpdateCaregiverCommand toCommand(Long caregiverId, UpdateCaregiverRequest request) {
        return new UpdateCaregiverCommand(
                caregiverId,
                request.firstName(),
                request.lastName(),
                request.age(),
                request.email(),
                request.phoneNumber(),
                request.imageUrl()
        );
    }
}
