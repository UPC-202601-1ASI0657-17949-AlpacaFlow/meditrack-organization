package com.alpacaflow.meditrack.organization.caregiver.application.internal.commandservices;

import com.alpacaflow.meditrack.organization.caregiver.domain.exceptions.CaregiverDuplicateRegistrationException;
import com.alpacaflow.meditrack.organization.caregiver.domain.exceptions.CaregiverInvalidRoleException;
import com.alpacaflow.meditrack.organization.caregiver.domain.exceptions.CaregiverNotFoundException;
import com.alpacaflow.meditrack.organization.caregiver.domain.model.aggregates.Caregiver;
import com.alpacaflow.meditrack.organization.caregiver.domain.model.commands.CreateCaregiverCommand;
import com.alpacaflow.meditrack.organization.caregiver.domain.model.commands.DeleteCaregiverCommand;
import com.alpacaflow.meditrack.organization.caregiver.domain.model.commands.UpdateCaregiverCommand;
import com.alpacaflow.meditrack.organization.caregiver.domain.services.CaregiverCommandService;
import com.alpacaflow.meditrack.organization.caregiver.infrastructure.persistence.jpa.repositories.CaregiverRepository;
import com.alpacaflow.meditrack.organization.organization.domain.exceptions.OrganizationNotFoundException;
import com.alpacaflow.meditrack.organization.organization.infrastructure.persistence.jpa.repositories.OrganizationRepository;
import com.alpacaflow.meditrack.organization.shared.application.internal.outboundservices.acl.IamContextFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

/**
 * Application service for write operations on the {@link Caregiver} aggregate.
 * <p>
 * Mirrors {@code DoctorCommandServiceImpl}: IAM lookups, role checks and user
 * autoprovisioning go through the {@link IamContextFacade}, keeping the service
 * decoupled from the actual IAM implementation (stub today, REST tomorrow).
 */
@Service
public class CaregiverCommandServiceImpl implements CaregiverCommandService {

    private static final String CAREGIVER_ROLE = "caregiver";

    private final CaregiverRepository caregiverRepository;
    private final OrganizationRepository organizationRepository;
    private final IamContextFacade iamContextFacade;

    public CaregiverCommandServiceImpl(CaregiverRepository caregiverRepository,
                                       OrganizationRepository organizationRepository,
                                       IamContextFacade iamContextFacade) {
        this.caregiverRepository = caregiverRepository;
        this.organizationRepository = organizationRepository;
        this.iamContextFacade = iamContextFacade;
    }

    @Override
    @Transactional
    public Long handle(CreateCaregiverCommand command) {
        var organization = organizationRepository.findById(command.organizationId())
                .orElseThrow(() -> new OrganizationNotFoundException(command.organizationId()));

        var canonicalEmail = normalizeEmail(command.email());
        assertNoDuplicateCaregiverInOrganization(
                organization.getId(),
                canonicalEmail,
                command.firstName(),
                command.lastName(),
                null);

        var resolvedUserId = resolveOrAutoprovisionUser(command.userId(), canonicalEmail);

        var caregiver = new Caregiver(
                organization,
                resolvedUserId,
                command.firstName(),
                command.lastName(),
                command.age(),
                canonicalEmail,
                command.phoneNumber(),
                command.imageUrl()
        );

        var saved = caregiverRepository.save(caregiver);
        saved.publishCreatedEvent();
        caregiverRepository.save(saved);
        return saved.getId();
    }

    @Override
    @Transactional
    public Optional<Caregiver> handle(UpdateCaregiverCommand command) {
        var caregiver = caregiverRepository.findById(command.caregiverId())
                .orElseThrow(() -> new CaregiverNotFoundException(command.caregiverId()));

        var canonicalEmail = normalizeEmail(command.email());
        assertNoDuplicateCaregiverInOrganization(
                caregiver.getOrganizationId(),
                canonicalEmail,
                command.firstName(),
                command.lastName(),
                command.caregiverId());

        caregiver.updatePersonalInformation(
                command.firstName(),
                command.lastName(),
                command.age(),
                canonicalEmail,
                command.phoneNumber()
        );
        if (command.imageUrl() != null && !command.imageUrl().isBlank()) {
            caregiver.updateImageUrl(command.imageUrl());
        }
        var linkedUserId = ensureIamUserForStaff(caregiver.getUserId(), canonicalEmail);
        if (!linkedUserId.equals(caregiver.getUserId())) {
            caregiver.updateUserId(linkedUserId);
        }
        return Optional.of(caregiverRepository.save(caregiver));
    }

    @Override
    @Transactional
    public void handle(DeleteCaregiverCommand command) {
        var caregiver = caregiverRepository.findById(command.caregiverId())
                .orElseThrow(() -> new CaregiverNotFoundException(command.caregiverId()));
        caregiver.markForDeletion();
        caregiverRepository.save(caregiver);
        caregiverRepository.deleteById(command.caregiverId());
    }

    /**
     * Ensures the staff member has a real IAM account (repairs caregivers created while IAM/JMS was unavailable).
     */
    private Long ensureIamUserForStaff(Long currentUserId, String canonicalEmail) {
        var existingByEmail = iamContextFacade.findUserByEmail(canonicalEmail);
        if (existingByEmail.isPresent()) {
            if (!iamContextFacade.userHasRole(existingByEmail.get().id(), CAREGIVER_ROLE)) {
                throw new CaregiverInvalidRoleException(existingByEmail.get().id(), existingByEmail.get().role());
            }
            return existingByEmail.get().id();
        }
        if (currentUserId != null && currentUserId > 0) {
            var byId = iamContextFacade.findUserById(currentUserId);
            if (byId.isPresent()) {
                if (!iamContextFacade.userHasRole(currentUserId, CAREGIVER_ROLE)) {
                    throw new CaregiverInvalidRoleException(currentUserId, byId.get().role());
                }
                return currentUserId;
            }
        }
        return iamContextFacade.createMockUser(canonicalEmail, CAREGIVER_ROLE);
    }

    private Long resolveOrAutoprovisionUser(Long providedUserId, String canonicalEmail) {
        if (providedUserId == null || providedUserId <= 0) {
            var existing = iamContextFacade.findUserByEmail(canonicalEmail);
            if (existing.isPresent()) {
                if (!iamContextFacade.userHasRole(existing.get().id(), CAREGIVER_ROLE)) {
                    throw new CaregiverInvalidRoleException(existing.get().id(), CAREGIVER_ROLE);
                }
                return existing.get().id();
            }
            return iamContextFacade.createMockUser(canonicalEmail, CAREGIVER_ROLE);
        }
        var user = iamContextFacade.findUserById(providedUserId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User with id %d not found in IAM".formatted(providedUserId)));
        if (!iamContextFacade.userHasRole(user.id(), CAREGIVER_ROLE)) {
            throw new CaregiverInvalidRoleException(user.id(), CAREGIVER_ROLE);
        }
        return user.id();
    }

    private void assertNoDuplicateCaregiverInOrganization(Long organizationId,
                                                          String email,
                                                          String firstName,
                                                          String lastName,
                                                          Long excludeCaregiverId) {
        var normalizedFirst = firstName == null ? "" : firstName.trim();
        var normalizedLast = lastName == null ? "" : lastName.trim();

        boolean duplicateEmail;
        boolean duplicateFullName;
        if (excludeCaregiverId == null) {
            duplicateEmail = caregiverRepository
                    .existsByOrganization_IdAndEmailIgnoreCase(organizationId, email);
            duplicateFullName = caregiverRepository
                    .existsByOrganization_IdAndFirstNameIgnoreCaseAndLastNameIgnoreCase(
                            organizationId, normalizedFirst, normalizedLast);
        } else {
            duplicateEmail = caregiverRepository
                    .existsByOrganization_IdAndEmailIgnoreCaseAndIdNot(organizationId, email, excludeCaregiverId);
            duplicateFullName = caregiverRepository
                    .existsByOrganization_IdAndFirstNameIgnoreCaseAndLastNameIgnoreCaseAndIdNot(
                            organizationId, normalizedFirst, normalizedLast, excludeCaregiverId);
        }

        if (duplicateEmail) {
            throw new CaregiverDuplicateRegistrationException(
                    CaregiverDuplicateRegistrationException.CODE_DUPLICATE_EMAIL,
                    "Another caregiver in this organization already uses this email.");
        }
        if (duplicateFullName) {
            throw new CaregiverDuplicateRegistrationException(
                    CaregiverDuplicateRegistrationException.CODE_DUPLICATE_FULL_NAME,
                    "Another caregiver in this organization already has this full name.");
        }
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
