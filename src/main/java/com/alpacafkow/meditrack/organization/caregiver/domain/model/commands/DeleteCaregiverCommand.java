package com.alpacafkow.meditrack.organization.caregiver.domain.model.commands;

public record DeleteCaregiverCommand(Long caregiverId) {
    public DeleteCaregiverCommand {
        if (caregiverId == null || caregiverId <= 0) {
            throw new IllegalArgumentException("caregiverId cannot be null or less than 1");
        }
    }
}
