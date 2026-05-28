package com.alpacaflow.meditrack.organization.seniorcitizen.infrastructure.persistence.jpa.repositories;

import com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.aggregates.SeniorCitizen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeniorCitizenRepository extends JpaRepository<SeniorCitizen, Long> {

    List<SeniorCitizen> findByOrganization_Id(Long organizationId);

    Optional<SeniorCitizen> findByIdAndOrganization_Id(Long id, Long organizationId);

    List<SeniorCitizen> findByAssignedDoctorId(Long doctorId);

    List<SeniorCitizen> findByAssignedCaregiverId(Long caregiverId);

    boolean existsByOrganization_IdAndDni(Long organizationId, String dni);

    boolean existsByOrganization_IdAndFirstNameIgnoreCaseAndLastNameIgnoreCase(
            Long organizationId, String firstName, String lastName);

    boolean existsByOrganization_IdAndDniAndIdNot(Long organizationId, String dni, Long id);

    boolean existsByOrganization_IdAndFirstNameIgnoreCaseAndLastNameIgnoreCaseAndIdNot(
            Long organizationId, String firstName, String lastName, Long id);

    boolean existsByDeviceId(Long deviceId);

    boolean existsByDeviceIdAndIdNot(Long deviceId, Long id);
}
