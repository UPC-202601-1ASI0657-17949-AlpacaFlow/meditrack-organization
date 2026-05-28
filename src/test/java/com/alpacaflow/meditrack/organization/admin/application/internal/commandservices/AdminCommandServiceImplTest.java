package com.alpacaflow.meditrack.organization.admin.application.internal.commandservices;

import com.alpacaflow.meditrack.organization.admin.domain.exceptions.AdminAlreadyExistsForUserException;
import com.alpacaflow.meditrack.organization.admin.domain.exceptions.AdminInvalidRoleException;
import com.alpacaflow.meditrack.organization.admin.domain.exceptions.AdminNotFoundException;
import com.alpacaflow.meditrack.organization.admin.domain.model.aggregates.Admin;
import com.alpacaflow.meditrack.organization.admin.domain.model.commands.CreateAdminCommand;
import com.alpacaflow.meditrack.organization.admin.domain.model.commands.DeleteAdminCommand;
import com.alpacaflow.meditrack.organization.admin.domain.model.commands.UpdateAdminCommand;
import com.alpacaflow.meditrack.organization.admin.infrastructure.persistence.jpa.repositories.AdminRepository;
import com.alpacaflow.meditrack.organization.organization.domain.exceptions.OrganizationNotFoundException;
import com.alpacaflow.meditrack.organization.organization.domain.model.aggregates.Organization;
import com.alpacaflow.meditrack.organization.organization.domain.model.valueobjects.Email;
import com.alpacaflow.meditrack.organization.organization.infrastructure.persistence.jpa.repositories.OrganizationRepository;
import com.alpacaflow.meditrack.organization.shared.application.internal.outboundservices.acl.ExternalUser;
import com.alpacaflow.meditrack.organization.shared.application.internal.outboundservices.acl.IamContextFacade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AdminCommandServiceImpl}.
 * <p>
 * The {@link IamContextFacade} dependency is mocked, demonstrating that the service does
 * not depend on the IAM bounded context implementation. This is the practical proof of the
 * Anti-Corruption Layer pattern: when the real IAM microservice is wired in, the same tests
 * keep working without modification.
 */
@ExtendWith(MockitoExtension.class)
class AdminCommandServiceImplTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private IamContextFacade iamContextFacade;

    @InjectMocks
    private AdminCommandServiceImpl commandService;

    @Test
    void shouldCreateAdminWhenAllPreconditionsAreMet() {
        var organization = new Organization("Clinica San Jose", "clinic", new Email("contact@clinic.example"));
        var command = new CreateAdminCommand(1L, 1001L, "Ana", "Torres");

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(iamContextFacade.findUserById(1001L))
                .thenReturn(Optional.of(new ExternalUser(1001L, "ana@iam.local", "admin")));
        when(iamContextFacade.userHasRole(1001L, "admin")).thenReturn(true);
        when(adminRepository.existsByUserIdAndOrganization_Id(1001L, null)).thenReturn(false);
        when(adminRepository.save(any(Admin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        commandService.handle(command);

        var captor = ArgumentCaptor.forClass(Admin.class);
        verify(adminRepository, atLeastOnce()).save(captor.capture());
        var savedAdmin = captor.getValue();
        assertEquals("Ana", savedAdmin.getFirstName());
        assertEquals("Torres", savedAdmin.getLastName());
        assertEquals(1001L, savedAdmin.getUserId());
        verify(iamContextFacade).findUserById(1001L);
        verify(iamContextFacade).userHasRole(1001L, "admin");
    }

    @Test
    void shouldThrowOrganizationNotFoundWhenOrganizationDoesNotExist() {
        var command = new CreateAdminCommand(99L, 1001L, "Ana", "Torres");
        when(organizationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(OrganizationNotFoundException.class, () -> commandService.handle(command));
        verify(adminRepository, never()).save(any(Admin.class));
    }

    @Test
    void shouldThrowIllegalArgumentWhenIamUserDoesNotExist() {
        var organization = new Organization("Clinica X", "clinic", new Email("x@clinic.example"));
        var command = new CreateAdminCommand(1L, 1001L, "Ana", "Torres");

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(iamContextFacade.findUserById(1001L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> commandService.handle(command));
        verify(adminRepository, never()).save(any(Admin.class));
    }

    @Test
    void shouldThrowInvalidRoleWhenIamUserDoesNotHaveAdminRole() {
        var organization = new Organization("Clinica X", "clinic", new Email("x@clinic.example"));
        var command = new CreateAdminCommand(1L, 1001L, "Ana", "Torres");

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(iamContextFacade.findUserById(1001L))
                .thenReturn(Optional.of(new ExternalUser(1001L, "u@iam.local", "doctor")));
        when(iamContextFacade.userHasRole(1001L, "admin")).thenReturn(false);

        assertThrows(AdminInvalidRoleException.class, () -> commandService.handle(command));
        verify(adminRepository, never()).save(any(Admin.class));
    }

    @Test
    void shouldThrowAdminAlreadyExistsWhenSameUserIsAlreadyAdminInOrganization() {
        var organization = new Organization("Clinica X", "clinic", new Email("x@clinic.example"));
        var command = new CreateAdminCommand(1L, 1001L, "Ana", "Torres");

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(iamContextFacade.findUserById(1001L))
                .thenReturn(Optional.of(new ExternalUser(1001L, "u@iam.local", "admin")));
        when(iamContextFacade.userHasRole(1001L, "admin")).thenReturn(true);
        when(adminRepository.existsByUserIdAndOrganization_Id(1001L, null)).thenReturn(true);

        assertThrows(AdminAlreadyExistsForUserException.class, () -> commandService.handle(command));
        verify(adminRepository, never()).save(any(Admin.class));
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingMissingAdmin() {
        var command = new UpdateAdminCommand(42L, "Ana", "Torres");
        when(adminRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(AdminNotFoundException.class, () -> commandService.handle(command));
        verify(adminRepository, never()).save(any(Admin.class));
    }

    @Test
    void shouldUpdateExistingAdmin() {
        var organization = new Organization("Clinica X", "clinic", new Email("x@clinic.example"));
        var existing = new Admin(organization, 1001L, "Ana", "Torres");
        var command = new UpdateAdminCommand(1L, "Ana Maria", "Torres Soto");

        when(adminRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(adminRepository.save(any(Admin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = commandService.handle(command);

        assertEquals("Ana Maria", result.orElseThrow().getFirstName());
        assertEquals("Torres Soto", result.orElseThrow().getLastName());
        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    void shouldThrowNotFoundWhenDeletingMissingAdmin() {
        var command = new DeleteAdminCommand(99L);
        when(adminRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(AdminNotFoundException.class, () -> commandService.handle(command));
        verify(adminRepository, never()).deleteById(any());
    }

    @Test
    void shouldDeleteAdminWhenItExists() {
        var organization = new Organization("Clinica X", "clinic", new Email("x@clinic.example"));
        var existing = new Admin(organization, 1001L, "Ana", "Torres");
        var command = new DeleteAdminCommand(1L);

        when(adminRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(adminRepository.save(any(Admin.class))).thenReturn(existing);
        doNothing().when(adminRepository).deleteById(1L);

        commandService.handle(command);

        verify(adminRepository).deleteById(1L);
    }
}
