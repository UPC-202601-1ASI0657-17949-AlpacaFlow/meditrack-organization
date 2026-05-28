package com.alpacaflow.meditrack.organization.organization.application.internal.commandservices;

import com.alpacaflow.meditrack.organization.organization.domain.exceptions.OrganizationDuplicateEmailException;
import com.alpacaflow.meditrack.organization.organization.domain.exceptions.OrganizationDuplicateNameException;
import com.alpacaflow.meditrack.organization.organization.domain.exceptions.OrganizationNotFoundException;
import com.alpacaflow.meditrack.organization.organization.domain.model.aggregates.Organization;
import com.alpacaflow.meditrack.organization.organization.domain.model.commands.CreateOrganizationCommand;
import com.alpacaflow.meditrack.organization.organization.domain.model.commands.DeleteOrganizationCommand;
import com.alpacaflow.meditrack.organization.organization.domain.model.commands.UpdateOrganizationCommand;
import com.alpacaflow.meditrack.organization.organization.domain.model.valueobjects.Email;
import com.alpacaflow.meditrack.organization.organization.infrastructure.persistence.jpa.repositories.OrganizationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationCommandServiceImplTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private OrganizationCommandServiceImpl commandService;

    @Test
    void shouldCreateOrganizationWhenDataIsValid() {
        var command = new CreateOrganizationCommand("Clinica A", "clinic", "contact@a.com");
        var saved = new Organization("Clinica A", "clinic", new Email("contact@a.com"));

        when(organizationRepository.existsByNameIgnoreCase("Clinica A")).thenReturn(false);
        when(organizationRepository.existsByEmail_Value("contact@a.com")).thenReturn(false);
        when(organizationRepository.save(any(Organization.class))).thenReturn(saved);

        var id = commandService.handle(command);

        assertEquals(saved.getId(), id);
        verify(organizationRepository, atLeastOnce()).save(any(Organization.class));
    }

    @Test
    void shouldThrowDuplicateNameExceptionWhenNameAlreadyExists() {
        var command = new CreateOrganizationCommand("Clinica A", "clinic", "contact@a.com");
        when(organizationRepository.existsByNameIgnoreCase("Clinica A")).thenReturn(true);

        assertThrows(OrganizationDuplicateNameException.class, () -> commandService.handle(command));
        verify(organizationRepository, never()).save(any(Organization.class));
    }

    @Test
    void shouldThrowDuplicateEmailExceptionWhenEmailAlreadyExists() {
        var command = new CreateOrganizationCommand("Clinica A", "clinic", "contact@a.com");
        when(organizationRepository.existsByNameIgnoreCase("Clinica A")).thenReturn(false);
        when(organizationRepository.existsByEmail_Value("contact@a.com")).thenReturn(true);

        assertThrows(OrganizationDuplicateEmailException.class, () -> commandService.handle(command));
        verify(organizationRepository, never()).save(any(Organization.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdatingNonExistingOrganization() {
        var command = new UpdateOrganizationCommand(99L, "Clinica A", "clinic", "contact@a.com");
        when(organizationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(OrganizationNotFoundException.class, () -> commandService.handle(command));
    }

    @Test
    void shouldDeleteOrganizationWhenItExists() {
        var command = new DeleteOrganizationCommand(1L);
        when(organizationRepository.existsById(1L)).thenReturn(true);
        doNothing().when(organizationRepository).deleteById(1L);

        commandService.handle(command);

        verify(organizationRepository).deleteById(1L);
    }
}
