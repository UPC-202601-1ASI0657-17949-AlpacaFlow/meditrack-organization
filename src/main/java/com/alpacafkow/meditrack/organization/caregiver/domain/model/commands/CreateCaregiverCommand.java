package com.alpacafkow.meditrack.organization.caregiver.domain.model.commands;

import com.alpacafkow.meditrack.organization.caregiver.domain.model.CaregiverInputRules;

public record CreateCaregiverCommand(
        Long organizationId,
        Long userId,
        String firstName,
        String lastName,
        Integer age,
        String email,
        String phoneNumber,
        String imageUrl
) {
    public CreateCaregiverCommand {
        if (organizationId == null || organizationId <= 0) {
            throw new IllegalArgumentException("organizationId cannot be null or less than 1");
        }
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("firstName cannot be null or blank");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("lastName cannot be null or blank");
        }
        CaregiverInputRules.assertCaregiverAge(age);
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email cannot be null or blank");
        }
        CaregiverInputRules.assertCaregiverPhoneDigitsOnly(phoneNumber);
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("imageUrl cannot be null or blank");
        }
    }
}
