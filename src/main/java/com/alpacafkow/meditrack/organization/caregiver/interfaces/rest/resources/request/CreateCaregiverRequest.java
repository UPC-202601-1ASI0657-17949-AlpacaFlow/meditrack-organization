package com.alpacafkow.meditrack.organization.caregiver.interfaces.rest.resources.request;

import com.alpacafkow.meditrack.organization.caregiver.domain.model.CaregiverInputRules;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

@Schema(description = "Payload to create a caregiver within an organization")
public record CreateCaregiverRequest(
        @NotNull(message = "organizationId is required")
        @Positive(message = "organizationId must be positive")
        @Schema(example = "1") Long organizationId,

        @Schema(example = "1003",
                description = "Optional. If null or 0, a user will be autoprovisioned in IAM with role 'caregiver'.")
        Long userId,

        @NotBlank(message = "firstName is required")
        @Schema(example = "Lucia") String firstName,

        @NotBlank(message = "lastName is required")
        @Schema(example = "Torres") String lastName,

        @NotNull(message = "age is required")
        @Min(value = CaregiverInputRules.AGE_MIN, message = "age must be >= 21")
        @Max(value = CaregiverInputRules.AGE_MAX, message = "age must be <= 65")
        @Schema(example = "32") Integer age,

        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        @Schema(example = "lucia.torres@clinic.example") String email,

        @NotBlank(message = "phoneNumber is required")
        @Pattern(regexp = "^\\d+$", message = "phoneNumber must contain only digits")
        @Schema(example = "999111222") String phoneNumber,

        @NotBlank(message = "imageUrl is required")
        @Schema(example = "https://example.com/avatar.png") String imageUrl
) {
}
