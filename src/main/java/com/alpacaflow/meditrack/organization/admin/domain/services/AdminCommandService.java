package com.alpacaflow.meditrack.organization.admin.domain.services;

import com.alpacaflow.meditrack.organization.admin.domain.model.aggregates.Admin;
import com.alpacaflow.meditrack.organization.admin.domain.model.commands.CreateAdminCommand;
import com.alpacaflow.meditrack.organization.admin.domain.model.commands.DeleteAdminCommand;
import com.alpacaflow.meditrack.organization.admin.domain.model.commands.UpdateAdminCommand;

import java.util.Optional;

public interface AdminCommandService {

    Long handle(CreateAdminCommand command);

    Optional<Admin> handle(UpdateAdminCommand command);

    void handle(DeleteAdminCommand command);
}
