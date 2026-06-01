package com.alpacaflow.meditrack.organization.seniorcitizen.application.internal.commandservices;

import com.alpacaflow.meditrack.organization.caregiver.domain.exceptions.CaregiverNotFoundException;
import com.alpacaflow.meditrack.organization.caregiver.domain.model.aggregates.Caregiver;
import com.alpacaflow.meditrack.organization.caregiver.infrastructure.persistence.jpa.repositories.CaregiverRepository;
import com.alpacaflow.meditrack.organization.doctor.domain.exceptions.DoctorNotFoundException;
import com.alpacaflow.meditrack.organization.doctor.domain.model.aggregates.Doctor;
import com.alpacaflow.meditrack.organization.doctor.infrastructure.persistence.jpa.repositories.DoctorRepository;
import com.alpacaflow.meditrack.organization.organization.domain.exceptions.OrganizationNotFoundException;
import com.alpacaflow.meditrack.organization.organization.domain.model.aggregates.Organization;
import com.alpacaflow.meditrack.organization.organization.domain.model.valueobjects.Email;
import com.alpacaflow.meditrack.organization.organization.infrastructure.persistence.jpa.repositories.OrganizationRepository;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.exceptions.SeniorCitizenAssignmentException;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.exceptions.SeniorCitizenDuplicateRegistrationException;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.exceptions.SeniorCitizenNotFoundException;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.aggregates.SeniorCitizen;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.commands.AssignSeniorCitizenToCaregiverCommand;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.commands.AssignSeniorCitizenToDoctorCommand;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.commands.CreateSeniorCitizenCommand;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.commands.DeleteSeniorCitizenCommand;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.commands.UnassignSeniorCitizenFromDoctorCommand;
import com.alpacaflow.meditrack.organization.seniorcitizen.infrastructure.persistence.jpa.repositories.SeniorCitizenRepository;
import com.alpacaflow.meditrack.organization.shared.application.internal.outboundservices.acl.DeviceContextFacade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SeniorCitizenCommandServiceImpl}.
 * <p>
 * Demonstrates the Anti-Corruption Layer for the Devices bounded context: the
 * {@link DeviceContextFacade} dependency is mocked and the test suite stays valid when the
 * stub is later replaced by a REST adapter pointing to the real Devices microservice.
 */
@ExtendWith(MockitoExtension.class)
class SeniorCitizenCommandServiceImplTest {

    @Mock private SeniorCitizenRepository seniorCitizenRepository;
    @Mock private OrganizationRepository organizationRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private CaregiverRepository caregiverRepository;
    @Mock private DeviceContextFacade deviceContextFacade;
    @Mock private IndividualUsersOrganizationResolver individualUsersOrganizationResolver;

    @InjectMocks private SeniorCitizenCommandServiceImpl commandService;

    private static Date birthDateForAge(int years) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -years);
        return cal.getTime();
    }

    private Organization sampleOrganization() {
        return new Organization("Clinica San Jose", "clinic", new Email("contact@clinic.example"));
    }

    private CreateSeniorCitizenCommand sampleCreateCommand(Long deviceId) {
        return new CreateSeniorCitizenCommand(
                1L, "Maria", "Quispe", birthDateForAge(70),
                "Femenino", 62.5, "12345678", 160.0,
                "https://x/y.png", deviceId);
    }

    private SeniorCitizen sampleSeniorCitizen(Organization organization, Long deviceId) {
        return new SeniorCitizen(organization, "Maria", "Quispe", birthDateForAge(70),
                "Femenino", 62.5, "12345678", 160.0, "https://x/y.png", deviceId);
    }

    // ------------------------------------------------------------------
    // CRUD
    // ------------------------------------------------------------------

    @Test
    void shouldCreateSeniorCitizenAndAutoCreateDeviceWhenDeviceIdIsNull() {
        var organization = sampleOrganization();
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(deviceContextFacade.reserveNextDeviceId()).thenReturn(5001L);
        when(seniorCitizenRepository.save(any(SeniorCitizen.class))).thenAnswer(inv -> inv.getArgument(0));

        commandService.handle(sampleCreateCommand(null));

        var captor = ArgumentCaptor.forClass(SeniorCitizen.class);
        verify(seniorCitizenRepository, atLeastOnce()).save(captor.capture());
        var saved = captor.getValue();
        assertEquals(5001L, saved.getDeviceId());
        assertEquals("Maria", saved.getFirstName());
        verify(deviceContextFacade).reserveNextDeviceId();
        verify(deviceContextFacade).registerDeviceForSeniorCitizen(5001L);
        verify(deviceContextFacade, never()).createDeviceForSeniorCitizen();
    }

    @Test
    void shouldReuseProvidedDeviceIdWhenDeviceExists() {
        var organization = sampleOrganization();
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(deviceContextFacade.deviceExists(7777L)).thenReturn(true);
        when(seniorCitizenRepository.existsByDeviceId(7777L)).thenReturn(false);
        when(seniorCitizenRepository.save(any(SeniorCitizen.class))).thenAnswer(inv -> inv.getArgument(0));

        commandService.handle(sampleCreateCommand(7777L));

        var captor = ArgumentCaptor.forClass(SeniorCitizen.class);
        verify(seniorCitizenRepository, atLeastOnce()).save(captor.capture());
        assertEquals(7777L, captor.getValue().getDeviceId());
        verify(deviceContextFacade, never()).createDeviceForSeniorCitizen();
    }

    @Test
    void shouldUseProvidedDeviceIdWhenNotYetRegisteredInDevicesContext() {
        var organization = sampleOrganization();
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(deviceContextFacade.deviceExists(9999L)).thenReturn(false);
        when(seniorCitizenRepository.save(any(SeniorCitizen.class))).thenAnswer(inv -> inv.getArgument(0));

        commandService.handle(sampleCreateCommand(9999L));

        var captor = ArgumentCaptor.forClass(SeniorCitizen.class);
        verify(seniorCitizenRepository, atLeastOnce()).save(captor.capture());
        assertEquals(9999L, captor.getValue().getDeviceId());
        verify(deviceContextFacade).registerDeviceForSeniorCitizen(9999L);
        verify(deviceContextFacade, never()).createDeviceForSeniorCitizen();
        verify(deviceContextFacade, never()).reserveNextDeviceId();
    }

    @Test
    void shouldThrowOrganizationNotFoundWhenOrganizationDoesNotExist() {
        when(organizationRepository.findById(99L)).thenReturn(Optional.empty());
        var command = new CreateSeniorCitizenCommand(99L, "Maria", "Quispe", birthDateForAge(70),
                "Femenino", 62.5, "12345678", 160.0, "https://x/y.png", null);

        assertThrows(OrganizationNotFoundException.class, () -> commandService.handle(command));
        verify(seniorCitizenRepository, never()).save(any(SeniorCitizen.class));
    }

    @Test
    void shouldThrowDuplicateDniWhenAnotherSeniorUsesSameDniInOrganization() {
        var organization = sampleOrganization();
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(seniorCitizenRepository.existsByOrganization_IdAndDni(any(), eq("12345678"))).thenReturn(true);

        var ex = assertThrows(SeniorCitizenDuplicateRegistrationException.class,
                () -> commandService.handle(sampleCreateCommand(null)));
        assertEquals(SeniorCitizenDuplicateRegistrationException.CODE_DUPLICATE_DNI, ex.getCode());
    }

    @Test
    void shouldThrowDeviceAlreadyAssignedWhenProvidedDeviceLinkedToAnotherSenior() {
        var organization = sampleOrganization();
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(seniorCitizenRepository.existsByDeviceId(7777L)).thenReturn(true);

        var ex = assertThrows(SeniorCitizenDuplicateRegistrationException.class,
                () -> commandService.handle(sampleCreateCommand(7777L)));
        assertEquals(SeniorCitizenDuplicateRegistrationException.CODE_DEVICE_ALREADY_ASSIGNED, ex.getCode());
    }

    @Test
    void shouldThrowNotFoundWhenDeletingMissingSenior() {
        when(seniorCitizenRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(SeniorCitizenNotFoundException.class,
                () -> commandService.handle(new DeleteSeniorCitizenCommand(99L)));
        verify(seniorCitizenRepository, never()).deleteById(anyLong());
    }

    @Test
    void shouldDeleteSeniorWhenItExists() {
        var organization = sampleOrganization();
        var existing = sampleSeniorCitizen(organization, 5001L);
        when(seniorCitizenRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(seniorCitizenRepository.save(any(SeniorCitizen.class))).thenReturn(existing);

        commandService.handle(new DeleteSeniorCitizenCommand(1L));

        verify(seniorCitizenRepository).deleteById(1L);
    }

    // ------------------------------------------------------------------
    // Assignments
    // ------------------------------------------------------------------

    @Test
    void shouldAssignSeniorToDoctorWhenInSameOrganizationAndNotAssignedToCaregiver() {
        var organization = sampleOrganization();
        injectId(organization, 1L);
        var senior = sampleSeniorCitizen(organization, 5001L);
        var doctor = new Doctor(organization, 1002L, "Carlos", "Mendoza", 45,
                "carlos@clinic.example", "Cardiology", "999111", "https://x/d.png");
        injectId(doctor, 20L);
        when(seniorCitizenRepository.findById(10L)).thenReturn(Optional.of(senior));
        when(doctorRepository.findById(20L)).thenReturn(Optional.of(doctor));
        when(seniorCitizenRepository.save(any(SeniorCitizen.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = commandService.handle(new AssignSeniorCitizenToDoctorCommand(10L, 20L));

        assertTrue(result.isPresent());
        assertEquals(20L, result.get().getAssignedDoctorId());
    }

    @Test
    void shouldRejectAssigningToDoctorFromDifferentOrganization() {
        var organizationA = sampleOrganization();
        injectId(organizationA, 1L);
        var organizationB = new Organization("Other Clinic", "clinic", new Email("other@clinic.example"));
        injectId(organizationB, 2L);
        var senior = sampleSeniorCitizen(organizationA, 5001L);
        var doctor = new Doctor(organizationB, 1002L, "Carlos", "Mendoza", 45,
                "carlos@clinic.example", "Cardiology", "999111", "https://x/d.png");
        injectId(doctor, 20L);
        when(seniorCitizenRepository.findById(10L)).thenReturn(Optional.of(senior));
        when(doctorRepository.findById(20L)).thenReturn(Optional.of(doctor));

        var ex = assertThrows(SeniorCitizenAssignmentException.class,
                () -> commandService.handle(new AssignSeniorCitizenToDoctorCommand(10L, 20L)));
        assertEquals(SeniorCitizenAssignmentException.CODE_DIFFERENT_ORGANIZATION, ex.getCode());
        verify(seniorCitizenRepository, never()).save(any(SeniorCitizen.class));
    }

    @Test
    void shouldRejectAssigningToDoctorWhenAlreadyAssignedToCaregiver() {
        var organization = sampleOrganization();
        injectId(organization, 1L);
        var senior = sampleSeniorCitizen(organization, 5001L);
        // Pre-assign the senior to a caregiver (id is whatever, business rule only checks existence)
        senior.assignToCaregiver(50L, organization.getId());

        var doctor = new Doctor(organization, 1002L, "Carlos", "Mendoza", 45,
                "carlos@clinic.example", "Cardiology", "999111", "https://x/d.png");
        injectId(doctor, 20L);
        when(seniorCitizenRepository.findById(10L)).thenReturn(Optional.of(senior));
        when(doctorRepository.findById(20L)).thenReturn(Optional.of(doctor));

        var ex = assertThrows(SeniorCitizenAssignmentException.class,
                () -> commandService.handle(new AssignSeniorCitizenToDoctorCommand(10L, 20L)));
        assertEquals(SeniorCitizenAssignmentException.CODE_ALREADY_ASSIGNED_TO_OTHER_TYPE, ex.getCode());
    }

    @Test
    void shouldThrowDoctorNotFoundWhenAssigning() {
        var organization = sampleOrganization();
        injectId(organization, 1L);
        var senior = sampleSeniorCitizen(organization, 5001L);
        when(seniorCitizenRepository.findById(10L)).thenReturn(Optional.of(senior));
        when(doctorRepository.findById(20L)).thenReturn(Optional.empty());

        assertThrows(DoctorNotFoundException.class,
                () -> commandService.handle(new AssignSeniorCitizenToDoctorCommand(10L, 20L)));
    }

    @Test
    void shouldUnassignSeniorFromDoctor() {
        var organization = sampleOrganization();
        injectId(organization, 1L);
        var senior = sampleSeniorCitizen(organization, 5001L);
        senior.assignToDoctor(20L, organization.getId());
        var doctor = new Doctor(organization, 1002L, "Carlos", "Mendoza", 45,
                "carlos@clinic.example", "Cardiology", "999111", "https://x/d.png");
        injectId(doctor, 20L);
        when(seniorCitizenRepository.findById(10L)).thenReturn(Optional.of(senior));
        when(doctorRepository.findById(20L)).thenReturn(Optional.of(doctor));
        when(seniorCitizenRepository.save(any(SeniorCitizen.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = commandService.handle(new UnassignSeniorCitizenFromDoctorCommand(10L, 20L));

        assertTrue(result.isPresent());
        assertNull(result.get().getAssignedDoctorId());
    }

    @Test
    void shouldAssignSeniorToCaregiverWhenInSameOrganizationAndNotAssignedToDoctor() {
        var organization = sampleOrganization();
        injectId(organization, 1L);
        var senior = sampleSeniorCitizen(organization, 5001L);
        var caregiver = new Caregiver(organization, 1003L, "Lucia", "Torres", 32,
                "lucia@clinic.example", "999111", "https://x/c.png");
        injectId(caregiver, 30L);
        when(seniorCitizenRepository.findById(10L)).thenReturn(Optional.of(senior));
        when(caregiverRepository.findById(30L)).thenReturn(Optional.of(caregiver));
        when(seniorCitizenRepository.save(any(SeniorCitizen.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = commandService.handle(new AssignSeniorCitizenToCaregiverCommand(10L, 30L));

        assertTrue(result.isPresent());
        assertEquals(30L, result.get().getAssignedCaregiverId());
    }

    @Test
    void shouldThrowCaregiverNotFoundWhenAssigning() {
        var organization = sampleOrganization();
        injectId(organization, 1L);
        var senior = sampleSeniorCitizen(organization, 5001L);
        when(seniorCitizenRepository.findById(10L)).thenReturn(Optional.of(senior));
        when(caregiverRepository.findById(30L)).thenReturn(Optional.empty());

        assertThrows(CaregiverNotFoundException.class,
                () -> commandService.handle(new AssignSeniorCitizenToCaregiverCommand(10L, 30L)));
    }

    /**
     * Aggregate ids are normally assigned by JPA; for unit tests covering multi-tenant
     * checks and assignment flows we set the inherited {@code id} field via reflection.
     * Walks the inheritance chain because the {@code id} field lives in
     * {@code AuditableAbstractAggregateRoot}.
     */
    private static void injectId(Object entity, Long id) {
        Class<?> type = entity.getClass();
        while (type != null) {
            try {
                var field = type.getDeclaredField("id");
                field.setAccessible(true);
                field.set(entity, id);
                return;
            } catch (NoSuchFieldException e) {
                type = type.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        throw new IllegalStateException("No 'id' field found in " + entity.getClass());
    }
}
