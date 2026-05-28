package com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.commands;

public record AssignSeniorCitizenToCaregiverCommand(Long seniorCitizenId, Long caregiverId) {
    public AssignSeniorCitizenToCaregiverCommand {
        if (seniorCitizenId == null || seniorCitizenId <= 0) {
            throw new IllegalArgumentException("seniorCitizenId cannot be null or less than 1");
        }
        if (caregiverId == null || caregiverId <= 0) {
            throw new IllegalArgumentException("caregiverId cannot be null or less than 1");
        }
    }
}
