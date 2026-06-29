package com.alpacaflow.meditrack.organization.seniorcitizen.application.internal.commandservices;

import com.alpacaflow.meditrack.organization.caregiver.domain.exceptions.CaregiverNotFoundException;
import com.alpacaflow.meditrack.organization.caregiver.infrastructure.persistence.jpa.repositories.CaregiverRepository;
import com.alpacaflow.meditrack.organization.doctor.domain.exceptions.DoctorNotFoundException;
import com.alpacaflow.meditrack.organization.doctor.infrastructure.persistence.jpa.repositories.DoctorRepository;
import com.alpacaflow.meditrack.organization.organization.domain.exceptions.OrganizationNotFoundException;
import com.alpacaflow.meditrack.organization.organization.domain.model.aggregates.Organization;
import com.alpacaflow.meditrack.organization.organization.infrastructure.persistence.jpa.repositories.OrganizationRepository;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.exceptions.DeviceUnavailableException;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.exceptions.SeniorCitizenAssignmentException;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.exceptions.SeniorCitizenDuplicateRegistrationException;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.exceptions.SeniorCitizenNotFoundException;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.aggregates.SeniorCitizen;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.commands.AssignSeniorCitizenToCaregiverCommand;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.commands.AssignSeniorCitizenToDoctorCommand;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.commands.CreateSeniorCitizenCommand;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.commands.DeleteSeniorCitizenCommand;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.commands.UnassignSeniorCitizenFromCaregiverCommand;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.commands.UnassignSeniorCitizenFromDoctorCommand;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.commands.UpdateSeniorCitizenCommand;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.services.SeniorCitizenCommandService;
import com.alpacaflow.meditrack.organization.seniorcitizen.infrastructure.persistence.jpa.repositories.SeniorCitizenRepository;
import com.alpacaflow.meditrack.organization.shared.application.internal.outboundservices.acl.DeviceContextFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;

/**
 * Application service for write operations on the {@link SeniorCitizen} aggregate.
 * <p>
 * Coordinates with three external pieces:
 * <ul>
 *     <li>{@code OrganizationRepository} (own bounded context) for tenancy validation.</li>
 *     <li>{@code DoctorRepository} / {@code CaregiverRepository} (own bounded context) for assignment operations.</li>
 *     <li>{@link DeviceContextFacade} (Anti-Corruption Layer) for the Devices bounded context.</li>
 * </ul>
 * <p>
 * Compared to the monolith, this implementation drops the redundant
 * {@code DoctorAssignment} / {@code CaregiverAssignment} JPA entities: the senior citizen
 * aggregate is the single source of truth for the assignment relationship, removing a
 * synchronization point and an entire pair of tables.
 */
@Service
public class SeniorCitizenCommandServiceImpl implements SeniorCitizenCommandService {

    private final SeniorCitizenRepository seniorCitizenRepository;
    private final OrganizationRepository organizationRepository;
    private final DoctorRepository doctorRepository;
    private final CaregiverRepository caregiverRepository;
    private final DeviceContextFacade deviceContextFacade;
    private final IndividualUsersOrganizationResolver individualUsersOrganizationResolver;

    public SeniorCitizenCommandServiceImpl(SeniorCitizenRepository seniorCitizenRepository,
                                           OrganizationRepository organizationRepository,
                                           DoctorRepository doctorRepository,
                                           CaregiverRepository caregiverRepository,
                                           DeviceContextFacade deviceContextFacade,
                                           IndividualUsersOrganizationResolver individualUsersOrganizationResolver) {
        this.seniorCitizenRepository = seniorCitizenRepository;
        this.organizationRepository = organizationRepository;
        this.doctorRepository = doctorRepository;
        this.caregiverRepository = caregiverRepository;
        this.deviceContextFacade = deviceContextFacade;
        this.individualUsersOrganizationResolver = individualUsersOrganizationResolver;
    }

    @Override
    @Transactional
    public Long handle(CreateSeniorCitizenCommand command) {
        var organization = resolveOrganization(command.organizationId());

        assertNoDuplicateInOrganization(
                organization.getId(),
                command.firstName(),
                command.lastName(),
                command.dni(),
                null);

        Long finalDeviceId = resolveDeviceIdForCreate(command.deviceId());
        assertDeviceNotLinkedToAnotherSenior(finalDeviceId, null);

        var seniorCitizen = new SeniorCitizen(
                organization,
                command.firstName(),
                command.lastName(),
                command.birthDate(),
                command.gender(),
                command.weight(),
                command.dni(),
                command.height(),
                command.imageUrl(),
                finalDeviceId
        );
        var saved = seniorCitizenRepository.save(seniorCitizen);
        saved.publishCreatedEvent();
        seniorCitizenRepository.save(saved);
        scheduleDeviceRegistrationAfterCommit(finalDeviceId, command.deviceId(), saved.getId());
        return saved.getId();
    }

    @Override
    @Transactional
    public Optional<SeniorCitizen> handle(UpdateSeniorCitizenCommand command) {
        var seniorCitizen = seniorCitizenRepository.findById(command.seniorCitizenId())
                .orElseThrow(() -> new SeniorCitizenNotFoundException(command.seniorCitizenId()));

        assertNoDuplicateInOrganization(
                seniorCitizen.getOrganizationId(),
                command.firstName(),
                command.lastName(),
                command.dni(),
                command.seniorCitizenId());

        seniorCitizen.updatePersonalInformation(
                command.firstName(),
                command.lastName(),
                command.birthDate(),
                command.gender(),
                command.weight(),
                command.dni(),
                command.height(),
                command.imageUrl()
        );

        if (!command.deviceId().equals(seniorCitizen.getDeviceId())) {
            assertDeviceNotLinkedToAnotherSenior(command.deviceId(), command.seniorCitizenId());
            seniorCitizen.updateDeviceId(command.deviceId());
        }

        var saved = seniorCitizenRepository.save(seniorCitizen);
        scheduleDeviceRegistrationAfterCommit(saved.getDeviceId(), command.deviceId(), saved.getId());
        return Optional.of(saved);
    }

    @Override
    @Transactional
    public void handle(DeleteSeniorCitizenCommand command) {
        var seniorCitizen = seniorCitizenRepository.findById(command.seniorCitizenId())
                .orElseThrow(() -> new SeniorCitizenNotFoundException(command.seniorCitizenId()));
        seniorCitizen.markForDeletion();
        seniorCitizenRepository.save(seniorCitizen);
        seniorCitizenRepository.deleteById(command.seniorCitizenId());
    }

    @Override
    @Transactional
    public Optional<SeniorCitizen> handle(AssignSeniorCitizenToDoctorCommand command) {
        var seniorCitizen = seniorCitizenRepository.findById(command.seniorCitizenId())
                .orElseThrow(() -> new SeniorCitizenNotFoundException(command.seniorCitizenId()));
        var doctor = doctorRepository.findById(command.doctorId())
                .orElseThrow(() -> new DoctorNotFoundException(command.doctorId()));
        try {
            seniorCitizen.assignToDoctor(doctor.getId(), doctor.getOrganizationId());
        } catch (IllegalStateException e) {
            throw new SeniorCitizenAssignmentException(
                    seniorCitizen.belongsToOrganization(doctor.getOrganizationId())
                            ? SeniorCitizenAssignmentException.CODE_ALREADY_ASSIGNED_TO_OTHER_TYPE
                            : SeniorCitizenAssignmentException.CODE_DIFFERENT_ORGANIZATION,
                    e.getMessage());
        }
        return Optional.of(seniorCitizenRepository.save(seniorCitizen));
    }

    @Override
    @Transactional
    public Optional<SeniorCitizen> handle(UnassignSeniorCitizenFromDoctorCommand command) {
        var seniorCitizen = seniorCitizenRepository.findById(command.seniorCitizenId())
                .orElseThrow(() -> new SeniorCitizenNotFoundException(command.seniorCitizenId()));
        var doctor = doctorRepository.findById(command.doctorId())
                .orElseThrow(() -> new DoctorNotFoundException(command.doctorId()));
        try {
            seniorCitizen.unassignFromDoctor(doctor.getId(), doctor.getOrganizationId());
        } catch (IllegalStateException e) {
            throw new SeniorCitizenAssignmentException(
                    SeniorCitizenAssignmentException.CODE_DIFFERENT_ORGANIZATION, e.getMessage());
        }
        return Optional.of(seniorCitizenRepository.save(seniorCitizen));
    }

    @Override
    @Transactional
    public Optional<SeniorCitizen> handle(AssignSeniorCitizenToCaregiverCommand command) {
        var seniorCitizen = seniorCitizenRepository.findById(command.seniorCitizenId())
                .orElseThrow(() -> new SeniorCitizenNotFoundException(command.seniorCitizenId()));
        var caregiver = caregiverRepository.findById(command.caregiverId())
                .orElseThrow(() -> new CaregiverNotFoundException(command.caregiverId()));
        try {
            seniorCitizen.assignToCaregiver(caregiver.getId(), caregiver.getOrganizationId());
        } catch (IllegalStateException e) {
            throw new SeniorCitizenAssignmentException(
                    seniorCitizen.belongsToOrganization(caregiver.getOrganizationId())
                            ? SeniorCitizenAssignmentException.CODE_ALREADY_ASSIGNED_TO_OTHER_TYPE
                            : SeniorCitizenAssignmentException.CODE_DIFFERENT_ORGANIZATION,
                    e.getMessage());
        }
        return Optional.of(seniorCitizenRepository.save(seniorCitizen));
    }

    @Override
    @Transactional
    public Optional<SeniorCitizen> handle(UnassignSeniorCitizenFromCaregiverCommand command) {
        var seniorCitizen = seniorCitizenRepository.findById(command.seniorCitizenId())
                .orElseThrow(() -> new SeniorCitizenNotFoundException(command.seniorCitizenId()));
        var caregiver = caregiverRepository.findById(command.caregiverId())
                .orElseThrow(() -> new CaregiverNotFoundException(command.caregiverId()));
        try {
            seniorCitizen.unassignFromCaregiver(caregiver.getId(), caregiver.getOrganizationId());
        } catch (IllegalStateException e) {
            throw new SeniorCitizenAssignmentException(
                    SeniorCitizenAssignmentException.CODE_DIFFERENT_ORGANIZATION, e.getMessage());
        }
        return Optional.of(seniorCitizenRepository.save(seniorCitizen));
    }

    // ----------------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------------

    /**
     * Resolves the device id to persist without registering the device in the Devices context.
     * Registration is deferred until after the senior citizen is saved successfully.
     */
    private Long resolveDeviceIdForCreate(Long providedDeviceId) {
        if (providedDeviceId == null || providedDeviceId == 0L) {
            return reserveNextDeviceId();
        }
        return providedDeviceId;
    }

    private Long reserveNextDeviceId() {
        try {
            Long reserved = deviceContextFacade.reserveNextDeviceId();
            if (reserved == null || reserved <= 0) {
                throw new DeviceUnavailableException("Devices context returned an invalid id");
            }
            return reserved;
        } catch (DeviceUnavailableException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new DeviceUnavailableException("Failed to reserve device id: " + e.getMessage());
        }
    }

    private void scheduleDeviceRegistrationAfterCommit(Long resolvedDeviceId, Long providedDeviceId, Long seniorCitizenId) {
        if (seniorCitizenId == null || seniorCitizenId <= 0) {
            return;
        }

        Runnable registerDevice = () -> deviceContextFacade.registerDeviceForSeniorCitizen(resolvedDeviceId, seniorCitizenId);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    registerDevice.run();
                }
            });
        } else {
            registerDevice.run();
        }
    }

    private void assertDeviceNotLinkedToAnotherSenior(Long deviceId, Long excludeSeniorCitizenId) {
        if (deviceId == null || deviceId <= 0) {
            return;
        }
        boolean taken = excludeSeniorCitizenId == null
                ? seniorCitizenRepository.existsByDeviceId(deviceId)
                : seniorCitizenRepository.existsByDeviceIdAndIdNot(deviceId, excludeSeniorCitizenId);
        if (taken) {
            throw new SeniorCitizenDuplicateRegistrationException(
                    SeniorCitizenDuplicateRegistrationException.CODE_DEVICE_ALREADY_ASSIGNED,
                    "Device %d is already assigned to another senior citizen".formatted(deviceId));
        }
    }

    private void assertNoDuplicateInOrganization(Long organizationId,
                                                 String firstName,
                                                 String lastName,
                                                 String dni,
                                                 Long excludeSeniorCitizenId) {
        var normalizedFirst = firstName == null ? "" : firstName.trim();
        var normalizedLast = lastName == null ? "" : lastName.trim();
        var normalizedDni = dni == null ? "" : dni.trim();

        boolean duplicateDni;
        boolean duplicateFullName;
        if (excludeSeniorCitizenId == null) {
            duplicateDni = seniorCitizenRepository
                    .existsByOrganization_IdAndDni(organizationId, normalizedDni);
            duplicateFullName = seniorCitizenRepository
                    .existsByOrganization_IdAndFirstNameIgnoreCaseAndLastNameIgnoreCase(
                            organizationId, normalizedFirst, normalizedLast);
        } else {
            duplicateDni = seniorCitizenRepository
                    .existsByOrganization_IdAndDniAndIdNot(organizationId, normalizedDni, excludeSeniorCitizenId);
            duplicateFullName = seniorCitizenRepository
                    .existsByOrganization_IdAndFirstNameIgnoreCaseAndLastNameIgnoreCaseAndIdNot(
                            organizationId, normalizedFirst, normalizedLast, excludeSeniorCitizenId);
        }

        if (duplicateDni) {
            throw new SeniorCitizenDuplicateRegistrationException(
                    SeniorCitizenDuplicateRegistrationException.CODE_DUPLICATE_DNI,
                    "Another senior citizen in this organization already has this DNI.");
        }
        if (duplicateFullName) {
            throw new SeniorCitizenDuplicateRegistrationException(
                    SeniorCitizenDuplicateRegistrationException.CODE_DUPLICATE_FULL_NAME,
                    "Another senior citizen in this organization already has this full name.");
        }
    }

    private Organization resolveOrganization(
            Long organizationId) {
        if (organizationId != null && organizationId == 0) {
            return individualUsersOrganizationResolver.resolve();
        }
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException(organizationId));
    }
}
