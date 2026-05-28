package com.alpacaflow.meditrack.organization.admin.domain.services;

import com.alpacaflow.meditrack.organization.admin.domain.model.aggregates.Admin;
import com.alpacaflow.meditrack.organization.admin.domain.model.queries.GetAdminByIdQuery;
import com.alpacaflow.meditrack.organization.admin.domain.model.queries.GetAdminByUserIdAndOrganizationIdQuery;
import com.alpacaflow.meditrack.organization.admin.domain.model.queries.GetAllAdminsByOrganizationIdQuery;
import com.alpacaflow.meditrack.organization.admin.domain.model.queries.GetAllAdminsQuery;

import java.util.List;
import java.util.Optional;

public interface AdminQueryService {

    Optional<Admin> handle(GetAdminByIdQuery query);

    List<Admin> handle(GetAllAdminsQuery query);

    List<Admin> handle(GetAllAdminsByOrganizationIdQuery query);

    Optional<Admin> handle(GetAdminByUserIdAndOrganizationIdQuery query);
}
