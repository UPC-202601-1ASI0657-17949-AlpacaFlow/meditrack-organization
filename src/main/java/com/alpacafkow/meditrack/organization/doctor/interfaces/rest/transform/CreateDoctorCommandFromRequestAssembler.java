package com.alpacafkow.meditrack.organization.doctor.interfaces.rest.transform;

import com.alpacafkow.meditrack.organization.doctor.domain.model.commands.CreateDoctorCommand;
import com.alpacafkow.meditrack.organization.doctor.interfaces.rest.resources.request.CreateDoctorRequest;

public final class CreateDoctorCommandFromRequestAssembler {

    private CreateDoctorCommandFromRequestAssembler() {
    }

    public static CreateDoctorCommand toCommand(CreateDoctorRequest request) {
        return new CreateDoctorCommand(
                request.organizationId(),
                request.userId(),
                request.firstName(),
                request.lastName(),
                request.specialty(),
                request.age(),
                request.email(),
                request.phoneNumber(),
                request.imageUrl()
        );
    }
}
