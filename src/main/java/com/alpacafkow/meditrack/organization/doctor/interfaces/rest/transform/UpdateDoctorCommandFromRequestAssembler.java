package com.alpacafkow.meditrack.organization.doctor.interfaces.rest.transform;

import com.alpacafkow.meditrack.organization.doctor.domain.model.commands.UpdateDoctorCommand;
import com.alpacafkow.meditrack.organization.doctor.interfaces.rest.resources.request.UpdateDoctorRequest;

public final class UpdateDoctorCommandFromRequestAssembler {

    private UpdateDoctorCommandFromRequestAssembler() {
    }

    public static UpdateDoctorCommand toCommand(Long doctorId, UpdateDoctorRequest request) {
        return new UpdateDoctorCommand(
                doctorId,
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
