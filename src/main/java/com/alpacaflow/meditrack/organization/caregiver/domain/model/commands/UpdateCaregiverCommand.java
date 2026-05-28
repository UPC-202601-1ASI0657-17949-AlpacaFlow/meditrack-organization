package com.alpacaflow.meditrack.organization.caregiver.domain.model.commands;

import com.alpacaflow.meditrack.organization.caregiver.domain.model.CaregiverInputRules;

public record UpdateCaregiverCommand(
        Long caregiverId,
        String firstName,
        String lastName,
        Integer age,
        String email,
        String phoneNumber,
        String imageUrl
) {
    public UpdateCaregiverCommand {
        if (caregiverId == null || caregiverId <= 0) {
            throw new IllegalArgumentException("caregiverId cannot be null or less than 1");
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
    }
}
