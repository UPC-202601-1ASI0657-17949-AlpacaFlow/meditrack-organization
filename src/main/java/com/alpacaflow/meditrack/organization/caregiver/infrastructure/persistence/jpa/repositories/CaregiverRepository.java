package com.alpacaflow.meditrack.organization.caregiver.infrastructure.persistence.jpa.repositories;

import com.alpacaflow.meditrack.organization.caregiver.domain.model.aggregates.Caregiver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaregiverRepository extends JpaRepository<Caregiver, Long> {

    List<Caregiver> findByOrganization_Id(Long organizationId);

    Optional<Caregiver> findByIdAndOrganization_Id(Long id, Long organizationId);

    Optional<Caregiver> findByUserId(Long userId);

    Optional<Caregiver> findByUserIdAndOrganization_Id(Long userId, Long organizationId);

    boolean existsByOrganization_IdAndEmailIgnoreCase(Long organizationId, String email);

    boolean existsByOrganization_IdAndFirstNameIgnoreCaseAndLastNameIgnoreCase(
            Long organizationId, String firstName, String lastName);

    boolean existsByOrganization_IdAndEmailIgnoreCaseAndIdNot(Long organizationId, String email, Long id);

    boolean existsByOrganization_IdAndFirstNameIgnoreCaseAndLastNameIgnoreCaseAndIdNot(
            Long organizationId, String firstName, String lastName, Long id);
}
