package com.alpacaflow.meditrack.organization.caregiver.domain.services;

import com.alpacaflow.meditrack.organization.caregiver.domain.model.aggregates.Caregiver;
import com.alpacaflow.meditrack.organization.caregiver.domain.model.commands.CreateCaregiverCommand;
import com.alpacaflow.meditrack.organization.caregiver.domain.model.commands.DeleteCaregiverCommand;
import com.alpacaflow.meditrack.organization.caregiver.domain.model.commands.UpdateCaregiverCommand;

import java.util.Optional;

public interface CaregiverCommandService {

    Long handle(CreateCaregiverCommand command);

    Optional<Caregiver> handle(UpdateCaregiverCommand command);

    void handle(DeleteCaregiverCommand command);
}
