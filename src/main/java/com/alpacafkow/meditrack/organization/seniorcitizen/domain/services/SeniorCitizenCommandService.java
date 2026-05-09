package com.alpacafkow.meditrack.organization.seniorcitizen.domain.services;

import com.alpacafkow.meditrack.organization.seniorcitizen.domain.model.aggregates.SeniorCitizen;
import com.alpacafkow.meditrack.organization.seniorcitizen.domain.model.commands.AssignSeniorCitizenToCaregiverCommand;
import com.alpacafkow.meditrack.organization.seniorcitizen.domain.model.commands.AssignSeniorCitizenToDoctorCommand;
import com.alpacafkow.meditrack.organization.seniorcitizen.domain.model.commands.CreateSeniorCitizenCommand;
import com.alpacafkow.meditrack.organization.seniorcitizen.domain.model.commands.DeleteSeniorCitizenCommand;
import com.alpacafkow.meditrack.organization.seniorcitizen.domain.model.commands.UnassignSeniorCitizenFromCaregiverCommand;
import com.alpacafkow.meditrack.organization.seniorcitizen.domain.model.commands.UnassignSeniorCitizenFromDoctorCommand;
import com.alpacafkow.meditrack.organization.seniorcitizen.domain.model.commands.UpdateSeniorCitizenCommand;

import java.util.Optional;

public interface SeniorCitizenCommandService {

    Long handle(CreateSeniorCitizenCommand command);

    Optional<SeniorCitizen> handle(UpdateSeniorCitizenCommand command);

    void handle(DeleteSeniorCitizenCommand command);

    Optional<SeniorCitizen> handle(AssignSeniorCitizenToDoctorCommand command);

    Optional<SeniorCitizen> handle(UnassignSeniorCitizenFromDoctorCommand command);

    Optional<SeniorCitizen> handle(AssignSeniorCitizenToCaregiverCommand command);

    Optional<SeniorCitizen> handle(UnassignSeniorCitizenFromCaregiverCommand command);
}
