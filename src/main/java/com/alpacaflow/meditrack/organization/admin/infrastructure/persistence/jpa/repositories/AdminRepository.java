package com.alpacaflow.meditrack.organization.admin.infrastructure.persistence.jpa.repositories;

import com.alpacaflow.meditrack.organization.admin.domain.model.aggregates.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    List<Admin> findByOrganization_Id(Long organizationId);

    Optional<Admin> findByUserIdAndOrganization_Id(Long userId, Long organizationId);

    boolean existsByUserIdAndOrganization_Id(Long userId, Long organizationId);
}
