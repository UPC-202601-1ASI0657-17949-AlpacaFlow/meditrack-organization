package com.alpacafkow.meditrack.organization.doctor.domain.services;

import com.alpacafkow.meditrack.organization.doctor.domain.model.aggregates.Doctor;
import com.alpacafkow.meditrack.organization.doctor.domain.model.queries.GetAllDoctorsByOrganizationIdQuery;
import com.alpacafkow.meditrack.organization.doctor.domain.model.queries.GetAllDoctorsQuery;
import com.alpacafkow.meditrack.organization.doctor.domain.model.queries.GetDoctorByIdQuery;
import com.alpacafkow.meditrack.organization.doctor.domain.model.queries.GetDoctorByUserIdAndOrganizationIdQuery;
import com.alpacafkow.meditrack.organization.doctor.domain.model.queries.GetDoctorByUserIdQuery;

import java.util.List;
import java.util.Optional;

public interface DoctorQueryService {

    Optional<Doctor> handle(GetDoctorByIdQuery query);

    List<Doctor> handle(GetAllDoctorsQuery query);

    List<Doctor> handle(GetAllDoctorsByOrganizationIdQuery query);

    Optional<Doctor> handle(GetDoctorByUserIdQuery query);

    Optional<Doctor> handle(GetDoctorByUserIdAndOrganizationIdQuery query);
}
