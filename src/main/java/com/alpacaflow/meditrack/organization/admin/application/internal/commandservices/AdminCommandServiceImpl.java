package com.alpacaflow.meditrack.organization.admin.application.internal.commandservices;

import com.alpacaflow.meditrack.organization.admin.domain.exceptions.AdminAlreadyExistsForUserException;
import com.alpacaflow.meditrack.organization.admin.domain.exceptions.AdminInvalidRoleException;
import com.alpacaflow.meditrack.organization.admin.domain.exceptions.AdminNotFoundException;
import com.alpacaflow.meditrack.organization.admin.domain.model.aggregates.Admin;
import com.alpacaflow.meditrack.organization.admin.domain.model.commands.CreateAdminCommand;
import com.alpacaflow.meditrack.organization.admin.domain.model.commands.DeleteAdminCommand;
import com.alpacaflow.meditrack.organization.admin.domain.model.commands.UpdateAdminCommand;
import com.alpacaflow.meditrack.organization.admin.domain.services.AdminCommandService;
import com.alpacaflow.meditrack.organization.admin.infrastructure.persistence.jpa.repositories.AdminRepository;
import com.alpacaflow.meditrack.organization.organization.domain.exceptions.OrganizationNotFoundException;
import com.alpacaflow.meditrack.organization.organization.infrastructure.persistence.jpa.repositories.OrganizationRepository;
import com.alpacaflow.meditrack.organization.shared.application.internal.outboundservices.acl.IamContextFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Application service for write operations on the {@link Admin} aggregate.
 * <p>
 * IAM-related responsibilities (existence and role of the underlying user) are delegated to
 * the {@link IamContextFacade}, so this service is unaware of whether IAM is mocked locally
 * or backed by a remote microservice. This is the Anti-Corruption Layer in action.
 */
@Service
public class AdminCommandServiceImpl implements AdminCommandService {

    private static final String ADMIN_ROLE = "admin";

    private final AdminRepository adminRepository;
    private final OrganizationRepository organizationRepository;
    private final IamContextFacade iamContextFacade;

    public AdminCommandServiceImpl(AdminRepository adminRepository,
                                   OrganizationRepository organizationRepository,
                                   IamContextFacade iamContextFacade) {
        this.adminRepository = adminRepository;
        this.organizationRepository = organizationRepository;
        this.iamContextFacade = iamContextFacade;
    }

    @Override
    @Transactional
    public Long handle(CreateAdminCommand command) {
        var organization = organizationRepository.findById(command.organizationId())
                .orElseThrow(() -> new OrganizationNotFoundException(command.organizationId()));

        var externalUser = iamContextFacade.findUserById(command.userId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "User with id %d not found in IAM".formatted(command.userId())));

        if (!iamContextFacade.userHasRole(externalUser.id(), ADMIN_ROLE)) {
            throw new AdminInvalidRoleException(externalUser.id());
        }

        if (adminRepository.existsByUserIdAndOrganization_Id(externalUser.id(), organization.getId())) {
            throw new AdminAlreadyExistsForUserException(externalUser.id(), organization.getId());
        }

        var admin = new Admin(organization, externalUser.id(), command.firstName(), command.lastName());
        var saved = adminRepository.save(admin);
        saved.publishCreatedEvent();
        adminRepository.save(saved);
        return saved.getId();
    }

    @Override
    @Transactional
    public Optional<Admin> handle(UpdateAdminCommand command) {
        var admin = adminRepository.findById(command.adminId())
                .orElseThrow(() -> new AdminNotFoundException(command.adminId()));

        admin.updatePersonalInformation(command.firstName(), command.lastName());
        return Optional.of(adminRepository.save(admin));
    }

    @Override
    @Transactional
    public void handle(DeleteAdminCommand command) {
        var admin = adminRepository.findById(command.adminId())
                .orElseThrow(() -> new AdminNotFoundException(command.adminId()));
        admin.markForDeletion();
        adminRepository.save(admin);
        adminRepository.deleteById(command.adminId());
    }
}
