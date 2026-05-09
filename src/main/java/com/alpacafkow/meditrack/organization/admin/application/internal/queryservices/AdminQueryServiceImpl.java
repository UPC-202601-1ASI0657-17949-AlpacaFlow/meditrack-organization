package com.alpacafkow.meditrack.organization.admin.application.internal.queryservices;

import com.alpacafkow.meditrack.organization.admin.domain.model.aggregates.Admin;
import com.alpacafkow.meditrack.organization.admin.domain.model.queries.GetAdminByIdQuery;
import com.alpacafkow.meditrack.organization.admin.domain.model.queries.GetAdminByUserIdAndOrganizationIdQuery;
import com.alpacafkow.meditrack.organization.admin.domain.model.queries.GetAllAdminsByOrganizationIdQuery;
import com.alpacafkow.meditrack.organization.admin.domain.model.queries.GetAllAdminsQuery;
import com.alpacafkow.meditrack.organization.admin.domain.services.AdminQueryService;
import com.alpacafkow.meditrack.organization.admin.infrastructure.persistence.jpa.repositories.AdminRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AdminQueryServiceImpl implements AdminQueryService {

    private final AdminRepository adminRepository;

    public AdminQueryServiceImpl(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Admin> handle(GetAdminByIdQuery query) {
        return adminRepository.findById(query.adminId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Admin> handle(GetAllAdminsQuery query) {
        return adminRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Admin> handle(GetAllAdminsByOrganizationIdQuery query) {
        return adminRepository.findByOrganization_Id(query.organizationId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Admin> handle(GetAdminByUserIdAndOrganizationIdQuery query) {
        return adminRepository.findByUserIdAndOrganization_Id(query.userId(), query.organizationId());
    }
}
