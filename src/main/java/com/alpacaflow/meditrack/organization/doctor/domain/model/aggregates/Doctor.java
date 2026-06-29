package com.alpacaflow.meditrack.organization.doctor.domain.model.aggregates;

import com.alpacaflow.meditrack.organization.doctor.domain.model.events.DoctorCreatedEvent;
import com.alpacaflow.meditrack.organization.doctor.domain.model.events.DoctorDeletedEvent;
import com.alpacaflow.meditrack.organization.doctor.domain.model.events.DoctorUpdatedEvent;
import com.alpacaflow.meditrack.organization.organization.domain.model.aggregates.Organization;
import com.alpacaflow.meditrack.organization.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

/**
 * Doctor aggregate within the organization microservice.
 * <p>
 * Represents a medical doctor that belongs to one {@link Organization} and is linked to a
 * user account managed by the IAM bounded context (referenced through {@code userId}).
 * <p>
 * Assignments to senior citizens are deliberately not modeled here in Sprint 1: the
 * relationship will be owned by the {@code SeniorCitizen} aggregate, which will publish the
 * domain events that this aggregate may subscribe to in a later iteration.
 */
@Entity
@Table(name = "doctors")
@Getter
public class Doctor extends AuditableAbstractAggregateRoot<Doctor> {

    @ManyToOne
    @JoinColumn(name = "org_id", nullable = false)
    private Organization organization;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "age", nullable = false)
    private Integer age;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "specialty", nullable = false)
    private String specialty;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    protected Doctor() {
    }

    public Doctor(Organization organization, Long userId, String firstName, String lastName, Integer age,
                  String email, String specialty, String phoneNumber, String imageUrl) {
        this.organization = validateOrganization(organization);
        this.userId = validateUserId(userId);
        this.firstName = validateRequired(firstName, "firstName");
        this.lastName = validateRequired(lastName, "lastName");
        this.age = validateAge(age);
        this.email = validateRequired(email, "email");
        this.specialty = validateRequired(specialty, "specialty");
        this.phoneNumber = validateRequired(phoneNumber, "phoneNumber");
        this.imageUrl = validateRequired(imageUrl, "imageUrl");
    }

    public void publishCreatedEvent() {
        this.addDomainEvent(new DoctorCreatedEvent(this, this.getId(), this.getOrganizationId()));
    }

    public Doctor updatePersonalInformation(String firstName, String lastName, Integer age,
                                             String email, String phoneNumber) {
        this.firstName = validateRequired(firstName, "firstName");
        this.lastName = validateRequired(lastName, "lastName");
        this.age = validateAge(age);
        this.email = validateRequired(email, "email");
        this.phoneNumber = validateRequired(phoneNumber, "phoneNumber");
        this.addDomainEvent(new DoctorUpdatedEvent(this, this.getId(), this.getOrganizationId()));
        return this;
    }

    public Doctor updateSpecialty(String specialty) {
        this.specialty = validateRequired(specialty, "specialty");
        this.addDomainEvent(new DoctorUpdatedEvent(this, this.getId(), this.getOrganizationId()));
        return this;
    }

    public Doctor updateImageUrl(String imageUrl) {
        this.imageUrl = validateRequired(imageUrl, "imageUrl");
        this.addDomainEvent(new DoctorUpdatedEvent(this, this.getId(), this.getOrganizationId()));
        return this;
    }

    public Doctor updateUserId(Long userId) {
        this.userId = validateUserId(userId);
        this.addDomainEvent(new DoctorUpdatedEvent(this, this.getId(), this.getOrganizationId()));
        return this;
    }

    public void markForDeletion() {
        this.addDomainEvent(new DoctorDeletedEvent(this, this.getId(), this.getOrganizationId()));
    }

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

    private Organization validateOrganization(Organization value) {
        if (value == null) {
            throw new IllegalArgumentException("organization cannot be null");
        }
        return value;
    }

    private Long validateUserId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("userId must be a positive number");
        }
        return value;
    }

    private Integer validateAge(Integer value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("age must be a positive number");
        }
        return value;
    }

    private String validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or blank");
        }
        return value.trim();
    }
}
