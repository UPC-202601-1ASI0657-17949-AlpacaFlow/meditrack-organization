package com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.commands;

import com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.SeniorCitizenPersonalDataValidation;

import java.util.Date;

public record CreateSeniorCitizenCommand(
        Long organizationId,
        String firstName,
        String lastName,
        Date birthDate,
        String gender,
        Double weight,
        String dni,
        Double height,
        String imageUrl,
        Long deviceId
) {
    public CreateSeniorCitizenCommand {
        if (organizationId == null || organizationId <= 0) {
            throw new IllegalArgumentException("organizationId cannot be null or less than 1");
        }
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("firstName cannot be null or blank");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("lastName cannot be null or blank");
        }
        SeniorCitizenPersonalDataValidation.validatePersonalData(birthDate, gender, weight, height, dni);
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("imageUrl cannot be null or blank");
        }
        if (deviceId != null && deviceId <= 0) {
            throw new IllegalArgumentException("deviceId must be greater than 0 if provided");
        }
    }
}
