package com.alpacaflow.meditrack.organization.admin.domain.model.aggregates;

import com.alpacaflow.meditrack.organization.admin.domain.model.events.AdminCreatedEvent;
import com.alpacaflow.meditrack.organization.admin.domain.model.events.AdminDeletedEvent;
import com.alpacaflow.meditrack.organization.admin.domain.model.events.AdminUpdatedEvent;
import com.alpacaflow.meditrack.organization.organization.domain.model.aggregates.Organization;
import com.alpacaflow.meditrack.organization.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

/**
 * Admin aggregate within the organization microservice.
 * <p>
 * Represents an administrator that belongs to a single {@link Organization} and is linked
 * to a user account managed by the IAM bounded context (referenced through {@code userId}
 * to keep the bounded contexts decoupled).
 */
@Entity
@Table(
        name = "admins",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_admin_user_per_org", columnNames = {"user_id", "org_id"})
        }
)
@Getter
public class Admin extends AuditableAbstractAggregateRoot<Admin> {

    @ManyToOne
    @JoinColumn(name = "org_id", nullable = false)
    private Organization organization;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    protected Admin() {
    }

    public Admin(Organization organization, Long userId, String firstName, String lastName) {
        this.organization = validateOrganization(organization);
        this.userId = validateUserId(userId);
        this.firstName = validateName(firstName, "firstName");
        this.lastName = validateName(lastName, "lastName");
    }

    public void publishCreatedEvent() {
        this.addDomainEvent(new AdminCreatedEvent(this, this.getId(), this.getOrganizationId()));
    }

    public Admin updatePersonalInformation(String firstName, String lastName) {
        this.firstName = validateName(firstName, "firstName");
        this.lastName = validateName(lastName, "lastName");
        this.addDomainEvent(new AdminUpdatedEvent(this, this.getId(), this.getOrganizationId()));
        return this;
    }

    public void markForDeletion() {
        this.addDomainEvent(new AdminDeletedEvent(this, this.getId(), this.getOrganizationId()));
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

    private String validateName(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or blank");
        }
        return value.trim();
    }
}
