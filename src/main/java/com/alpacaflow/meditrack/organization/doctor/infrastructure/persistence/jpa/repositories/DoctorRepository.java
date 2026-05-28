package com.alpacaflow.meditrack.organization.doctor.infrastructure.persistence.jpa.repositories;

import com.alpacaflow.meditrack.organization.doctor.domain.model.aggregates.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    List<Doctor> findByOrganization_Id(Long organizationId);

    Optional<Doctor> findByIdAndOrganization_Id(Long id, Long organizationId);

    Optional<Doctor> findByUserId(Long userId);

    Optional<Doctor> findByUserIdAndOrganization_Id(Long userId, Long organizationId);

    boolean existsByOrganization_IdAndEmailIgnoreCase(Long organizationId, String email);

    boolean existsByOrganization_IdAndFirstNameIgnoreCaseAndLastNameIgnoreCase(
            Long organizationId, String firstName, String lastName);

    boolean existsByOrganization_IdAndEmailIgnoreCaseAndIdNot(Long organizationId, String email, Long id);

    boolean existsByOrganization_IdAndFirstNameIgnoreCaseAndLastNameIgnoreCaseAndIdNot(
            Long organizationId, String firstName, String lastName, Long id);
}
