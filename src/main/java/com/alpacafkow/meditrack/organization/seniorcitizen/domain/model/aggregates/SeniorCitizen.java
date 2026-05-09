package com.alpacafkow.meditrack.organization.seniorcitizen.domain.model.aggregates;

import com.alpacafkow.meditrack.organization.organization.domain.model.aggregates.Organization;
import com.alpacafkow.meditrack.organization.seniorcitizen.domain.model.SeniorCitizenPersonalDataValidation;
import com.alpacafkow.meditrack.organization.seniorcitizen.domain.model.events.SeniorCitizenAssignedToCaregiverEvent;
import com.alpacafkow.meditrack.organization.seniorcitizen.domain.model.events.SeniorCitizenAssignedToDoctorEvent;
import com.alpacafkow.meditrack.organization.seniorcitizen.domain.model.events.SeniorCitizenCreatedEvent;
import com.alpacafkow.meditrack.organization.seniorcitizen.domain.model.events.SeniorCitizenDeletedEvent;
import com.alpacafkow.meditrack.organization.seniorcitizen.domain.model.events.SeniorCitizenUnassignedFromCaregiverEvent;
import com.alpacafkow.meditrack.organization.seniorcitizen.domain.model.events.SeniorCitizenUnassignedFromDoctorEvent;
import com.alpacafkow.meditrack.organization.seniorcitizen.domain.model.events.SeniorCitizenUpdatedEvent;
import com.alpacafkow.meditrack.organization.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

import java.util.Date;

/**
 * SeniorCitizen aggregate root.
 * <p>
 * Owns the assignment relationships with {@code Doctor} and {@code Caregiver} as scalar
 * foreign keys ({@code assignedDoctorId} / {@code assignedCaregiverId}). The monolith
 * additionally kept separate {@code DoctorAssignment} / {@code CaregiverAssignment} entities
 * that duplicated this state — they are intentionally not migrated to the microservice
 * (single source of truth is the aggregate itself).
 * <p>
 * The senior citizen is also linked to a {@code deviceId} owned by the Devices bounded
 * context (validated through the {@code DeviceContextFacade} at the application layer).
 */
@Entity
@Table(name = "senior_citizens")
@Getter
public class SeniorCitizen extends AuditableAbstractAggregateRoot<SeniorCitizen> {

    @ManyToOne
    @JoinColumn(name = "org_id", nullable = false)
    private Organization organization;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "birth_date", nullable = false)
    private Date birthDate;

    @Column(name = "age", nullable = false)
    private Integer age;

    @Column(name = "gender", nullable = false)
    private String gender;

    @Column(name = "weight", nullable = false)
    private Double weight;

    @Column(name = "dni", nullable = false)
    private String dni;

    @Column(name = "height", nullable = false)
    private Double height;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "device_id", nullable = false, unique = true)
    private Long deviceId;

    @Column(name = "assigned_doctor_id")
    private Long assignedDoctorId;

    @Column(name = "assigned_caregiver_id")
    private Long assignedCaregiverId;

    protected SeniorCitizen() {
    }

    public SeniorCitizen(Organization organization, String firstName, String lastName, Date birthDate,
                         String gender, Double weight, String dni, Double height, String imageUrl, Long deviceId) {
        if (organization == null) {
            throw new IllegalArgumentException("organization cannot be null");
        }
        SeniorCitizenPersonalDataValidation.validatePersonalData(birthDate, gender, weight, height, dni);
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("firstName cannot be null or blank");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("lastName cannot be null or blank");
        }
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("imageUrl cannot be null or blank");
        }
        if (deviceId == null || deviceId <= 0) {
            throw new IllegalArgumentException("deviceId must be a positive number");
        }

        this.organization = organization;
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
        this.birthDate = birthDate;
        this.age = SeniorCitizenPersonalDataValidation.calculateAgeYears(birthDate);
        this.gender = SeniorCitizenPersonalDataValidation.normalizeGender(gender);
        this.weight = weight;
        this.dni = dni.trim();
        this.height = height;
        this.imageUrl = imageUrl.trim();
        this.deviceId = deviceId;
    }

    public void publishCreatedEvent() {
        this.addDomainEvent(new SeniorCitizenCreatedEvent(this, this.getId(), this.getOrganizationId()));
    }

    public SeniorCitizen updatePersonalInformation(String firstName, String lastName, Date birthDate,
                                                   String gender, Double weight, String dni, Double height, String imageUrl) {
        SeniorCitizenPersonalDataValidation.validatePersonalData(birthDate, gender, weight, height, dni);
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("firstName cannot be null or blank");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("lastName cannot be null or blank");
        }
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("imageUrl cannot be null or blank");
        }
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
        this.birthDate = birthDate;
        this.age = SeniorCitizenPersonalDataValidation.calculateAgeYears(birthDate);
        this.gender = SeniorCitizenPersonalDataValidation.normalizeGender(gender);
        this.weight = weight;
        this.dni = dni.trim();
        this.height = height;
        this.imageUrl = imageUrl.trim();
        this.addDomainEvent(new SeniorCitizenUpdatedEvent(this, this.getId(), this.getOrganizationId()));
        return this;
    }

    public SeniorCitizen updateDeviceId(Long deviceId) {
        if (deviceId == null || deviceId <= 0) {
            throw new IllegalArgumentException("deviceId must be a positive number");
        }
        this.deviceId = deviceId;
        this.addDomainEvent(new SeniorCitizenUpdatedEvent(this, this.getId(), this.getOrganizationId()));
        return this;
    }

    public void markForDeletion() {
        this.addDomainEvent(new SeniorCitizenDeletedEvent(this, this.getId(), this.getOrganizationId()));
    }

    // ---------- Multi-tenant ----------

    public Long getOrganizationId() {
        return organization != null ? organization.getId() : null;
    }

    public String getFullName() {
        return String.format("%s %s", firstName, lastName).trim();
    }

    public boolean belongsToOrganization(Long organizationId) {
        return this.organization != null && this.organization.getId() != null
                && this.organization.getId().equals(organizationId);
    }

    // ---------- Assignments (mutual exclusion: doctor XOR caregiver) ----------

    public boolean canBeAssignedToDoctor() {
        return this.assignedCaregiverId == null;
    }

    public boolean canBeAssignedToCaregiver() {
        return this.assignedDoctorId == null;
    }

    public boolean isAssignedToAnyDoctor() {
        return this.assignedDoctorId != null;
    }

    public boolean isAssignedToAnyCaregiver() {
        return this.assignedCaregiverId != null;
    }

    public boolean isAssignedToDoctor(Long doctorId) {
        return this.assignedDoctorId != null && this.assignedDoctorId.equals(doctorId);
    }

    public boolean isAssignedToCaregiver(Long caregiverId) {
        return this.assignedCaregiverId != null && this.assignedCaregiverId.equals(caregiverId);
    }

    public void assignToDoctor(Long doctorId, Long doctorOrganizationId) {
        assertSameOrganization("doctor", doctorOrganizationId);
        if (!canBeAssignedToDoctor()) {
            throw new IllegalStateException(
                    "Cannot assign senior citizen to doctor: senior citizen is already assigned to a caregiver. "
                            + "A senior citizen can only be assigned to a doctor OR a caregiver, not both.");
        }
        Long previousDoctorId = this.assignedDoctorId;
        if (previousDoctorId != null && !previousDoctorId.equals(doctorId)) {
            this.assignedDoctorId = null;
            this.addDomainEvent(new SeniorCitizenUnassignedFromDoctorEvent(
                    this, this.getId(), previousDoctorId, this.getOrganizationId()));
        }
        this.assignedDoctorId = doctorId;
        this.assignedCaregiverId = null;
        this.addDomainEvent(new SeniorCitizenAssignedToDoctorEvent(
                this, this.getId(), doctorId, this.getOrganizationId()));
    }

    public void unassignFromDoctor(Long doctorId, Long doctorOrganizationId) {
        assertSameOrganization("doctor", doctorOrganizationId);
        if (isAssignedToDoctor(doctorId)) {
            this.assignedDoctorId = null;
            this.addDomainEvent(new SeniorCitizenUnassignedFromDoctorEvent(
                    this, this.getId(), doctorId, this.getOrganizationId()));
        }
    }

    public void assignToCaregiver(Long caregiverId, Long caregiverOrganizationId) {
        assertSameOrganization("caregiver", caregiverOrganizationId);
        if (!canBeAssignedToCaregiver()) {
            throw new IllegalStateException(
                    "Cannot assign senior citizen to caregiver: senior citizen is already assigned to a doctor. "
                            + "A senior citizen can only be assigned to a doctor OR a caregiver, not both.");
        }
        Long previousCaregiverId = this.assignedCaregiverId;
        if (previousCaregiverId != null && !previousCaregiverId.equals(caregiverId)) {
            this.assignedCaregiverId = null;
            this.addDomainEvent(new SeniorCitizenUnassignedFromCaregiverEvent(
                    this, this.getId(), previousCaregiverId, this.getOrganizationId()));
        }
        this.assignedCaregiverId = caregiverId;
        this.assignedDoctorId = null;
        this.addDomainEvent(new SeniorCitizenAssignedToCaregiverEvent(
                this, this.getId(), caregiverId, this.getOrganizationId()));
    }

    public void unassignFromCaregiver(Long caregiverId, Long caregiverOrganizationId) {
        assertSameOrganization("caregiver", caregiverOrganizationId);
        if (isAssignedToCaregiver(caregiverId)) {
            this.assignedCaregiverId = null;
            this.addDomainEvent(new SeniorCitizenUnassignedFromCaregiverEvent(
                    this, this.getId(), caregiverId, this.getOrganizationId()));
        }
    }

    private void assertSameOrganization(String otherActorLabel, Long otherOrganizationId) {
        if (!belongsToOrganization(otherOrganizationId)) {
            throw new IllegalStateException(
                    "Senior citizen and %s belong to different organizations (senior=%d, %s=%d)"
                            .formatted(otherActorLabel, this.getOrganizationId(), otherActorLabel, otherOrganizationId));
        }
    }
}
