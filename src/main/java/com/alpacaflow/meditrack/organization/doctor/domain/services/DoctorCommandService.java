package com.alpacaflow.meditrack.organization.doctor.domain.services;

import com.alpacaflow.meditrack.organization.doctor.domain.model.aggregates.Doctor;
import com.alpacaflow.meditrack.organization.doctor.domain.model.commands.CreateDoctorCommand;
import com.alpacaflow.meditrack.organization.doctor.domain.model.commands.DeleteDoctorCommand;
import com.alpacaflow.meditrack.organization.doctor.domain.model.commands.UpdateDoctorCommand;

import java.util.Optional;

public interface DoctorCommandService {

    Long handle(CreateDoctorCommand command);

    Optional<Doctor> handle(UpdateDoctorCommand command);

    void handle(DeleteDoctorCommand command);
}
