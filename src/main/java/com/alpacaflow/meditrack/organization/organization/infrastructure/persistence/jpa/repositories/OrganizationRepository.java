package com.alpacaflow.meditrack.organization.organization.infrastructure.persistence.jpa.repositories;

import com.alpacaflow.meditrack.organization.organization.domain.model.aggregates.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    boolean existsByEmail_Value(String email);

    boolean existsByEmail_ValueAndIdNot(String email, Long id);
}
