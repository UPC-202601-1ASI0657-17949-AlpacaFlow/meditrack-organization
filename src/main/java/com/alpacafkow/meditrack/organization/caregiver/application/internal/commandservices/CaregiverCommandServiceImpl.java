package com.alpacafkow.meditrack.organization.caregiver.application.internal.commandservices;

import com.alpacafkow.meditrack.organization.caregiver.domain.exceptions.CaregiverDuplicateRegistrationException;
import com.alpacafkow.meditrack.organization.caregiver.domain.exceptions.CaregiverInvalidRoleException;
import com.alpacafkow.meditrack.organization.caregiver.domain.exceptions.CaregiverNotFoundException;
import com.alpacafkow.meditrack.organization.caregiver.domain.model.aggregates.Caregiver;
import com.alpacafkow.meditrack.organization.caregiver.domain.model.commands.CreateCaregiverCommand;
import com.alpacafkow.meditrack.organization.caregiver.domain.model.commands.DeleteCaregiverCommand;
import com.alpacafkow.meditrack.organization.caregiver.domain.model.commands.UpdateCaregiverCommand;
import com.alpacafkow.meditrack.organization.caregiver.domain.services.CaregiverCommandService;
import com.alpacafkow.meditrack.organization.caregiver.infrastructure.persistence.jpa.repositories.CaregiverRepository;
import com.alpacafkow.meditrack.organization.organization.domain.exceptions.OrganizationNotFoundException;
import com.alpacafkow.meditrack.organization.organization.infrastructure.persistence.jpa.repositories.OrganizationRepository;
import com.alpacafkow.meditrack.organization.shared.application.internal.outboundservices.acl.IamContextFacade;
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
