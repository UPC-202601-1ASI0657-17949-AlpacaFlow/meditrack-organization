package com.alpacaflow.meditrack.organization.seniorcitizen.interfaces.rest.transform;

import com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.commands.CreateSeniorCitizenCommand;
import com.alpacaflow.meditrack.organization.seniorcitizen.interfaces.rest.resources.request.CreateSeniorCitizenRequest;

import java.util.Locale;

public final class CreateSeniorCitizenCommandFromRequestAssembler {

    private CreateSeniorCitizenCommandFromRequestAssembler() {
    }

    public static CreateSeniorCitizenCommand toCommand(CreateSeniorCitizenRequest request) {
        return new CreateSeniorCitizenCommand(
                request.organizationId(),
                request.firstName(),
                request.lastName(),
                request.birthDate(),
                normalizeGender(request.gender()),
                request.weight(),
                request.dni(),
                request.height(),
                request.imageUrl(),
                request.deviceId()
        );
    }

    private static String normalizeGender(String gender) {
        if (gender == null) {
            return null;
        }
        return switch (gender.trim().toLowerCase(Locale.ROOT)) {
            case "male", "masculino", "m" -> "Masculino";
            case "female", "femenino", "f" -> "Femenino";
            default -> gender.trim();
        };
    }
}
