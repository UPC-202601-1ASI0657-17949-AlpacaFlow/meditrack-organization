package com.alpacaflow.meditrack.organization.caregiver.application.internal.commandservices;

import com.alpacaflow.meditrack.organization.caregiver.domain.exceptions.CaregiverDuplicateRegistrationException;
import com.alpacaflow.meditrack.organization.caregiver.domain.exceptions.CaregiverInvalidRoleException;
import com.alpacaflow.meditrack.organization.caregiver.domain.exceptions.CaregiverNotFoundException;
import com.alpacaflow.meditrack.organization.caregiver.domain.model.aggregates.Caregiver;
import com.alpacaflow.meditrack.organization.caregiver.domain.model.commands.CreateCaregiverCommand;
import com.alpacaflow.meditrack.organization.caregiver.domain.model.commands.DeleteCaregiverCommand;
import com.alpacaflow.meditrack.organization.caregiver.domain.model.commands.UpdateCaregiverCommand;
import com.alpacaflow.meditrack.organization.caregiver.infrastructure.persistence.jpa.repositories.CaregiverRepository;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CaregiverCommandServiceImpl}.
 * <p>
 * Mirrors the strategy used for {@code DoctorCommandServiceImpl}: the {@link IamContextFacade}
 * is mocked, so this entire suite stays green when the IAM stub is replaced by a remote REST
 * adapter without modifying business logic. This is the Anti-Corruption Layer pattern at work.
 */
@ExtendWith(MockitoExtension.class)
class CaregiverCommandServiceImplTest {

    @Mock
    private CaregiverRepository caregiverRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private IamContextFacade iamContextFacade;

    @InjectMocks
    private CaregiverCommandServiceImpl commandService;

    private Organization sampleOrganization() {
        return new Organization("Clinica San Jose", "clinic", new Email("contact@clinic.example"));
    }

    private CreateCaregiverCommand sampleCreateCommand(Long userId) {
        return new CreateCaregiverCommand(1L, userId, "Lucia", "Torres",
                32, "lucia@clinic.example", "999111222", "https://x/y.png");
    }

    @Test
    void shouldCreateCaregiverWithProvidedUserId() {
        var organization = sampleOrganization();
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(iamContextFacade.findUserById(1003L))
                .thenReturn(Optional.of(new ExternalUser(1003L, "lucia@clinic.example", "caregiver")));
        when(iamContextFacade.userHasRole(1003L, "caregiver")).thenReturn(true);
        when(caregiverRepository.save(any(Caregiver.class))).thenAnswer(inv -> inv.getArgument(0));

        commandService.handle(sampleCreateCommand(1003L));

        var captor = ArgumentCaptor.forClass(Caregiver.class);
        verify(caregiverRepository, atLeastOnce()).save(captor.capture());
        var saved = captor.getValue();
        assertEquals("Lucia", saved.getFirstName());
        assertEquals(1003L, saved.getUserId());
        assertEquals("lucia@clinic.example", saved.getEmail());
        assertEquals("999111222", saved.getPhoneNumber());
        verify(iamContextFacade).findUserById(1003L);
        verify(iamContextFacade).userHasRole(1003L, "caregiver");
    }

    @Test
    void shouldAutoprovisionUserWhenUserIdIsNull() {
        var organization = sampleOrganization();
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(iamContextFacade.findUserByEmail("lucia@clinic.example")).thenReturn(Optional.empty());
        when(iamContextFacade.createMockUser("lucia@clinic.example", "caregiver")).thenReturn(7777L);
        when(caregiverRepository.save(any(Caregiver.class))).thenAnswer(inv -> inv.getArgument(0));

        commandService.handle(sampleCreateCommand(null));

        var captor = ArgumentCaptor.forClass(Caregiver.class);
        verify(caregiverRepository, atLeastOnce()).save(captor.capture());
        assertEquals(7777L, captor.getValue().getUserId());
        verify(iamContextFacade).createMockUser("lucia@clinic.example", "caregiver");
    }

    @Test
    void shouldReuseExistingUserWhenAutoprovisioningAndEmailExists() {
        var organization = sampleOrganization();
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(iamContextFacade.findUserByEmail("lucia@clinic.example"))
                .thenReturn(Optional.of(new ExternalUser(8888L, "lucia@clinic.example", "caregiver")));
        when(iamContextFacade.userHasRole(8888L, "caregiver")).thenReturn(true);
        when(caregiverRepository.save(any(Caregiver.class))).thenAnswer(inv -> inv.getArgument(0));

        commandService.handle(sampleCreateCommand(0L));

        var captor = ArgumentCaptor.forClass(Caregiver.class);
        verify(caregiverRepository, atLeastOnce()).save(captor.capture());
        assertEquals(8888L, captor.getValue().getUserId());
        verify(iamContextFacade, never()).createMockUser(anyString(), anyString());
    }

    @Test
    void shouldThrowOrganizationNotFoundWhenOrganizationDoesNotExist() {
        when(organizationRepository.findById(99L)).thenReturn(Optional.empty());
        var command = new CreateCaregiverCommand(99L, 1003L, "Lucia", "Torres",
                32, "lucia@clinic.example", "999111222", "https://x/y.png");

        assertThrows(OrganizationNotFoundException.class, () -> commandService.handle(command));
        verify(caregiverRepository, never()).save(any(Caregiver.class));
    }

    @Test
    void shouldThrowDuplicateEmailWhenAnotherCaregiverUsesEmailInSameOrganization() {
        var organization = sampleOrganization();
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(caregiverRepository.existsByOrganization_IdAndEmailIgnoreCase(any(), eq("lucia@clinic.example")))
                .thenReturn(true);

        var ex = assertThrows(CaregiverDuplicateRegistrationException.class,
                () -> commandService.handle(sampleCreateCommand(1003L)));
        assertEquals(CaregiverDuplicateRegistrationException.CODE_DUPLICATE_EMAIL, ex.getCode());
        verify(caregiverRepository, never()).save(any(Caregiver.class));
    }

    @Test
    void shouldThrowInvalidRoleWhenIamUserDoesNotHaveCaregiverRole() {
        var organization = sampleOrganization();
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(iamContextFacade.findUserById(1003L))
                .thenReturn(Optional.of(new ExternalUser(1003L, "lucia@clinic.example", "doctor")));
        when(iamContextFacade.userHasRole(1003L, "caregiver")).thenReturn(false);

        assertThrows(CaregiverInvalidRoleException.class,
                () -> commandService.handle(sampleCreateCommand(1003L)));
        verify(caregiverRepository, never()).save(any(Caregiver.class));
    }

    @Test
    void shouldThrowIllegalArgumentWhenIamUserDoesNotExist() {
        var organization = sampleOrganization();
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(iamContextFacade.findUserById(1003L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> commandService.handle(sampleCreateCommand(1003L)));
        verify(caregiverRepository, never()).save(any(Caregiver.class));
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingMissingCaregiver() {
        when(caregiverRepository.findById(99L)).thenReturn(Optional.empty());
        var command = new UpdateCaregiverCommand(99L, "Lucia", "Torres", 33,
                "lucia@clinic.example", "999111222", "https://x/y.png");

        assertThrows(CaregiverNotFoundException.class, () -> commandService.handle(command));
    }

    @Test
    void shouldUpdateExistingCaregiver() {
        var organization = sampleOrganization();
        var existing = new Caregiver(organization, 1003L, "Lucia", "Torres", 32,
                "lucia@clinic.example", "999111222", "https://x/y.png");
        when(caregiverRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(caregiverRepository.save(any(Caregiver.class))).thenAnswer(inv -> inv.getArgument(0));

        var command = new UpdateCaregiverCommand(1L, "Lucia", "Torres Diaz", 33,
                "lucia@clinic.example", "988111222", "https://x/new.png");

        var result = commandService.handle(command);

        assertEquals("Torres Diaz", result.orElseThrow().getLastName());
        assertEquals(33, result.get().getAge());
        assertEquals("988111222", result.get().getPhoneNumber());
        verify(caregiverRepository, atLeastOnce()).save(any(Caregiver.class));
    }

    @Test
    void shouldThrowNotFoundWhenDeletingMissingCaregiver() {
        when(caregiverRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(CaregiverNotFoundException.class,
                () -> commandService.handle(new DeleteCaregiverCommand(99L)));
        verify(caregiverRepository, never()).deleteById(anyLong());
    }

    @Test
    void shouldDeleteCaregiverWhenItExists() {
        var organization = sampleOrganization();
        var existing = new Caregiver(organization, 1003L, "Lucia", "Torres", 32,
                "lucia@clinic.example", "999111222", "https://x/y.png");
        when(caregiverRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(caregiverRepository.save(any(Caregiver.class))).thenReturn(existing);

        commandService.handle(new DeleteCaregiverCommand(1L));

        verify(caregiverRepository).deleteById(1L);
    }
}
