package com.alpacafkow.meditrack.organization.doctor.application.internal.commandservices;

import com.alpacafkow.meditrack.organization.doctor.domain.exceptions.DoctorDuplicateRegistrationException;
import com.alpacafkow.meditrack.organization.doctor.domain.exceptions.DoctorInvalidRoleException;
import com.alpacafkow.meditrack.organization.doctor.domain.exceptions.DoctorNotFoundException;
import com.alpacafkow.meditrack.organization.doctor.domain.model.aggregates.Doctor;
import com.alpacafkow.meditrack.organization.doctor.domain.model.commands.CreateDoctorCommand;
import com.alpacafkow.meditrack.organization.doctor.domain.model.commands.DeleteDoctorCommand;
import com.alpacafkow.meditrack.organization.doctor.domain.model.commands.UpdateDoctorCommand;
import com.alpacafkow.meditrack.organization.doctor.infrastructure.persistence.jpa.repositories.DoctorRepository;
import com.alpacafkow.meditrack.organization.organization.domain.exceptions.OrganizationNotFoundException;
import com.alpacafkow.meditrack.organization.organization.domain.model.aggregates.Organization;
import com.alpacafkow.meditrack.organization.organization.domain.model.valueobjects.Email;
import com.alpacafkow.meditrack.organization.organization.infrastructure.persistence.jpa.repositories.OrganizationRepository;
import com.alpacafkow.meditrack.organization.shared.application.internal.outboundservices.acl.ExternalUser;
import com.alpacafkow.meditrack.organization.shared.application.internal.outboundservices.acl.IamContextFacade;
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
 * Unit tests for {@link DoctorCommandServiceImpl}.
 * <p>
 * The {@link IamContextFacade} dependency is mocked to demonstrate the Anti-Corruption Layer:
 * the service can be tested in isolation regardless of whether IAM is a stub or a remote
 * microservice. The same tests will keep working without modification when the REST adapter
 * is wired in.
 */
@ExtendWith(MockitoExtension.class)
class DoctorCommandServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private IamContextFacade iamContextFacade;

    @InjectMocks
    private DoctorCommandServiceImpl commandService;

    private Organization sampleOrganization() {
        return new Organization("Clinica San Jose", "clinic", new Email("contact@clinic.example"));
    }

    private CreateDoctorCommand sampleCreateCommand(Long userId) {
        return new CreateDoctorCommand(1L, userId, "Carlos", "Mendoza", "Cardiology",
                45, "carlos@clinic.example", "+51999999999", "https://x/y.png");
    }

    @Test
    void shouldCreateDoctorWithProvidedUserId() {
        var organization = sampleOrganization();
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(iamContextFacade.findUserById(1002L))
                .thenReturn(Optional.of(new ExternalUser(1002L, "carlos@clinic.example", "doctor")));
        when(iamContextFacade.userHasRole(1002L, "doctor")).thenReturn(true);
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(inv -> inv.getArgument(0));

        commandService.handle(sampleCreateCommand(1002L));

        var captor = ArgumentCaptor.forClass(Doctor.class);
        verify(doctorRepository, atLeastOnce()).save(captor.capture());
        var saved = captor.getValue();
        assertEquals("Carlos", saved.getFirstName());
        assertEquals(1002L, saved.getUserId());
        assertEquals("carlos@clinic.example", saved.getEmail());
        verify(iamContextFacade).findUserById(1002L);
        verify(iamContextFacade).userHasRole(1002L, "doctor");
    }

    @Test
    void shouldAutoprovisionUserWhenUserIdIsNull() {
        var organization = sampleOrganization();
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(iamContextFacade.findUserByEmail("carlos@clinic.example")).thenReturn(Optional.empty());
        when(iamContextFacade.createMockUser("carlos@clinic.example", "doctor")).thenReturn(7777L);
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(inv -> inv.getArgument(0));

        commandService.handle(sampleCreateCommand(null));

        var captor = ArgumentCaptor.forClass(Doctor.class);
        verify(doctorRepository, atLeastOnce()).save(captor.capture());
        assertEquals(7777L, captor.getValue().getUserId());
        verify(iamContextFacade).createMockUser("carlos@clinic.example", "doctor");
    }

    @Test
    void shouldReuseExistingUserWhenAutoprovisioningAndEmailExists() {
        var organization = sampleOrganization();
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(iamContextFacade.findUserByEmail("carlos@clinic.example"))
                .thenReturn(Optional.of(new ExternalUser(8888L, "carlos@clinic.example", "doctor")));
        when(iamContextFacade.userHasRole(8888L, "doctor")).thenReturn(true);
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(inv -> inv.getArgument(0));

        commandService.handle(sampleCreateCommand(0L));

        var captor = ArgumentCaptor.forClass(Doctor.class);
        verify(doctorRepository, atLeastOnce()).save(captor.capture());
        assertEquals(8888L, captor.getValue().getUserId());
        verify(iamContextFacade, never()).createMockUser(anyString(), anyString());
    }

    @Test
    void shouldThrowOrganizationNotFoundWhenOrganizationDoesNotExist() {
        when(organizationRepository.findById(99L)).thenReturn(Optional.empty());
        var command = new CreateDoctorCommand(99L, 1002L, "Carlos", "Mendoza", "Cardiology",
                45, "carlos@clinic.example", "+51999", "https://x/y.png");

        assertThrows(OrganizationNotFoundException.class, () -> commandService.handle(command));
        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    @Test
    void shouldThrowDuplicateEmailWhenAnotherDoctorUsesEmailInSameOrganization() {
        var organization = sampleOrganization();
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(doctorRepository.existsByOrganization_IdAndEmailIgnoreCase(any(), eq("carlos@clinic.example")))
                .thenReturn(true);

        var ex = assertThrows(DoctorDuplicateRegistrationException.class,
                () -> commandService.handle(sampleCreateCommand(1002L)));
        assertEquals(DoctorDuplicateRegistrationException.CODE_DUPLICATE_EMAIL, ex.getCode());
        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    @Test
    void shouldThrowInvalidRoleWhenIamUserDoesNotHaveDoctorRole() {
        var organization = sampleOrganization();
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(iamContextFacade.findUserById(1002L))
                .thenReturn(Optional.of(new ExternalUser(1002L, "carlos@clinic.example", "admin")));
        when(iamContextFacade.userHasRole(1002L, "doctor")).thenReturn(false);

        assertThrows(DoctorInvalidRoleException.class, () -> commandService.handle(sampleCreateCommand(1002L)));
        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    @Test
    void shouldThrowIllegalArgumentWhenIamUserDoesNotExist() {
        var organization = sampleOrganization();
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(iamContextFacade.findUserById(1002L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> commandService.handle(sampleCreateCommand(1002L)));
        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingMissingDoctor() {
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());
        var command = new UpdateDoctorCommand(99L, "Carlos", "Mendoza", "Cardiology", 46,
                "carlos@clinic.example", "+51999", "https://x/y.png");

        assertThrows(DoctorNotFoundException.class, () -> commandService.handle(command));
    }

    @Test
    void shouldUpdateExistingDoctor() {
        var organization = sampleOrganization();
        var existing = new Doctor(organization, 1002L, "Carlos", "Mendoza", 45,
                "carlos@clinic.example", "Cardiology", "+51999", "https://x/y.png");
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(inv -> inv.getArgument(0));

        var command = new UpdateDoctorCommand(1L, "Carlos", "Mendoza Soto", "Neurology", 46,
                "carlos@clinic.example", "+51888", "https://x/new.png");

        var result = commandService.handle(command);

        assertEquals("Mendoza Soto", result.orElseThrow().getLastName());
        assertEquals("Neurology", result.get().getSpecialty());
        assertEquals(46, result.get().getAge());
        verify(doctorRepository, atLeastOnce()).save(any(Doctor.class));
    }

    @Test
    void shouldThrowNotFoundWhenDeletingMissingDoctor() {
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(DoctorNotFoundException.class,
                () -> commandService.handle(new DeleteDoctorCommand(99L)));
        verify(doctorRepository, never()).deleteById(anyLong());
    }

    @Test
    void shouldDeleteDoctorWhenItExists() {
        var organization = sampleOrganization();
        var existing = new Doctor(organization, 1002L, "Carlos", "Mendoza", 45,
                "carlos@clinic.example", "Cardiology", "+51999", "https://x/y.png");
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(existing);

        commandService.handle(new DeleteDoctorCommand(1L));

        verify(doctorRepository).deleteById(1L);
    }
}
