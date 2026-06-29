package com.alpacaflow.meditrack.organization.doctor.application.internal.commandservices;

import com.alpacaflow.meditrack.organization.doctor.domain.exceptions.DoctorDuplicateRegistrationException;
import com.alpacaflow.meditrack.organization.doctor.domain.exceptions.DoctorInvalidRoleException;
import com.alpacaflow.meditrack.organization.doctor.domain.exceptions.DoctorNotFoundException;
import com.alpacaflow.meditrack.organization.doctor.domain.model.aggregates.Doctor;
import com.alpacaflow.meditrack.organization.doctor.domain.model.commands.CreateDoctorCommand;
import com.alpacaflow.meditrack.organization.doctor.domain.model.commands.DeleteDoctorCommand;
import com.alpacaflow.meditrack.organization.doctor.domain.model.commands.UpdateDoctorCommand;
import com.alpacaflow.meditrack.organization.doctor.domain.services.DoctorCommandService;
import com.alpacaflow.meditrack.organization.doctor.infrastructure.persistence.jpa.repositories.DoctorRepository;
import com.alpacaflow.meditrack.organization.organization.domain.exceptions.OrganizationNotFoundException;
import com.alpacaflow.meditrack.organization.organization.infrastructure.persistence.jpa.repositories.OrganizationRepository;
import com.alpacaflow.meditrack.organization.shared.application.internal.outboundservices.acl.IamContextFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

/**
 * Application service for write operations on the {@link Doctor} aggregate.
 * <p>
 * IAM lookups, role checks and user autoprovisioning are delegated to the
 * {@link IamContextFacade}, so the service is unaware of whether IAM is mocked locally
 * or backed by a remote microservice.
 */
@Service
public class DoctorCommandServiceImpl implements DoctorCommandService {

    private static final String DOCTOR_ROLE = "doctor";

    private final DoctorRepository doctorRepository;
    private final OrganizationRepository organizationRepository;
    private final IamContextFacade iamContextFacade;

    public DoctorCommandServiceImpl(DoctorRepository doctorRepository,
                                    OrganizationRepository organizationRepository,
                                    IamContextFacade iamContextFacade) {
        this.doctorRepository = doctorRepository;
        this.organizationRepository = organizationRepository;
        this.iamContextFacade = iamContextFacade;
    }

    @Override
    @Transactional
    public Long handle(CreateDoctorCommand command) {
        var organization = organizationRepository.findById(command.organizationId())
                .orElseThrow(() -> new OrganizationNotFoundException(command.organizationId()));

        var canonicalEmail = normalizeEmail(command.email());
        assertNoDuplicateDoctorInOrganization(
                organization.getId(),
                canonicalEmail,
                command.firstName(),
                command.lastName(),
                null);

        var resolvedUserId = resolveOrAutoprovisionUser(command.userId(), canonicalEmail);

        var doctor = new Doctor(
                organization,
                resolvedUserId,
                command.firstName(),
                command.lastName(),
                command.age(),
                canonicalEmail,
                command.specialty(),
                command.phoneNumber(),
                command.imageUrl()
        );

        var saved = doctorRepository.save(doctor);
        saved.publishCreatedEvent();
        doctorRepository.save(saved);
        return saved.getId();
    }

    @Override
    @Transactional
    public Optional<Doctor> handle(UpdateDoctorCommand command) {
        var doctor = doctorRepository.findById(command.doctorId())
                .orElseThrow(() -> new DoctorNotFoundException(command.doctorId()));

        var canonicalEmail = normalizeEmail(command.email());
        assertNoDuplicateDoctorInOrganization(
                doctor.getOrganizationId(),
                canonicalEmail,
                command.firstName(),
                command.lastName(),
                command.doctorId());

        doctor.updatePersonalInformation(
                command.firstName(),
                command.lastName(),
                command.age(),
                canonicalEmail,
                command.phoneNumber()
        );
        doctor.updateSpecialty(command.specialty());
        if (command.imageUrl() != null && !command.imageUrl().isBlank()) {
            doctor.updateImageUrl(command.imageUrl());
        }
        var linkedUserId = ensureIamUserForStaff(doctor.getUserId(), canonicalEmail);
        if (!linkedUserId.equals(doctor.getUserId())) {
            doctor.updateUserId(linkedUserId);
        }
        return Optional.of(doctorRepository.save(doctor));
    }

    @Override
    @Transactional
    public void handle(DeleteDoctorCommand command) {
        var doctor = doctorRepository.findById(command.doctorId())
                .orElseThrow(() -> new DoctorNotFoundException(command.doctorId()));
        doctor.markForDeletion();
        doctorRepository.save(doctor);
        doctorRepository.deleteById(command.doctorId());
    }

    /**
     * Ensures the staff member has a real IAM account (repairs doctors created while IAM/JMS was unavailable).
     */
    private Long ensureIamUserForStaff(Long currentUserId, String canonicalEmail) {
        var existingByEmail = iamContextFacade.findUserByEmail(canonicalEmail);
        if (existingByEmail.isPresent()) {
            if (!iamContextFacade.userHasRole(existingByEmail.get().id(), DOCTOR_ROLE)) {
                throw new DoctorInvalidRoleException(existingByEmail.get().id(), existingByEmail.get().role());
            }
            return existingByEmail.get().id();
        }
        if (currentUserId != null && currentUserId > 0) {
            var byId = iamContextFacade.findUserById(currentUserId);
            if (byId.isPresent()) {
                if (!iamContextFacade.userHasRole(currentUserId, DOCTOR_ROLE)) {
                    throw new DoctorInvalidRoleException(currentUserId, byId.get().role());
                }
                return currentUserId;
            }
        }
        return iamContextFacade.createMockUser(canonicalEmail, DOCTOR_ROLE);
    }

    private Long resolveOrAutoprovisionUser(Long providedUserId, String canonicalEmail) {
        if (providedUserId == null || providedUserId <= 0) {
            // Autoprovision via IAM facade — same behavior as the monolith
            var existing = iamContextFacade.findUserByEmail(canonicalEmail);
            if (existing.isPresent()) {
                if (!iamContextFacade.userHasRole(existing.get().id(), DOCTOR_ROLE)) {
                    throw new DoctorInvalidRoleException(existing.get().id(), existing.get().role());
                }
                return existing.get().id();
            }
            return iamContextFacade.createMockUser(canonicalEmail, DOCTOR_ROLE);
        }
        var user = iamContextFacade.findUserById(providedUserId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User with id %d not found in IAM".formatted(providedUserId)));
        if (!iamContextFacade.userHasRole(user.id(), DOCTOR_ROLE)) {
            throw new DoctorInvalidRoleException(user.id(), user.role());
        }
        return user.id();
    }

    private void assertNoDuplicateDoctorInOrganization(Long organizationId,
                                                       String email,
                                                       String firstName,
                                                       String lastName,
                                                       Long excludeDoctorId) {
        var normalizedFirst = firstName == null ? "" : firstName.trim();
        var normalizedLast = lastName == null ? "" : lastName.trim();

        boolean duplicateEmail;
        boolean duplicateFullName;
        if (excludeDoctorId == null) {
            duplicateEmail = doctorRepository
                    .existsByOrganization_IdAndEmailIgnoreCase(organizationId, email);
            duplicateFullName = doctorRepository
                    .existsByOrganization_IdAndFirstNameIgnoreCaseAndLastNameIgnoreCase(
                            organizationId, normalizedFirst, normalizedLast);
        } else {
            duplicateEmail = doctorRepository
                    .existsByOrganization_IdAndEmailIgnoreCaseAndIdNot(organizationId, email, excludeDoctorId);
            duplicateFullName = doctorRepository
                    .existsByOrganization_IdAndFirstNameIgnoreCaseAndLastNameIgnoreCaseAndIdNot(
                            organizationId, normalizedFirst, normalizedLast, excludeDoctorId);
        }

        if (duplicateEmail) {
            throw new DoctorDuplicateRegistrationException(
                    DoctorDuplicateRegistrationException.CODE_DUPLICATE_EMAIL,
                    "Another doctor in this organization already uses this email.");
        }
        if (duplicateFullName) {
            throw new DoctorDuplicateRegistrationException(
                    DoctorDuplicateRegistrationException.CODE_DUPLICATE_FULL_NAME,
                    "Another doctor in this organization already has this full name.");
        }
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
