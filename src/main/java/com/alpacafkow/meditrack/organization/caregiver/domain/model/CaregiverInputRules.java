package com.alpacafkow.meditrack.organization.caregiver.domain.model;

/**
 * Shared validation rules for caregiver registration (create / update).
 * <p>
 * Encapsulates business rules that are part of the Caregiver bounded context vocabulary so
 * they can be reused by commands, the aggregate and the REST validation layer without
 * duplicating logic.
 */
public final class CaregiverInputRules {

    public static final int AGE_MIN = 21;
    public static final int AGE_MAX = 65;

    private CaregiverInputRules() {
    }

    public static void assertCaregiverAge(Integer age) {
        if (age == null || age < AGE_MIN || age > AGE_MAX) {
            throw new IllegalArgumentException(
                    "Caregiver age must be between %d and %d (inclusive)".formatted(AGE_MIN, AGE_MAX));
        }
    }

    /**
     * Phone must be non-blank and contain only ASCII digits (no letters, spaces, or symbols).
     */
    public static void assertCaregiverPhoneDigitsOnly(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be null or blank");
        }
        var trimmed = phoneNumber.trim();
        for (int i = 0; i < trimmed.length(); i++) {
            if (!Character.isDigit(trimmed.charAt(i))) {
                throw new IllegalArgumentException(
                        "Phone number must contain only digits (no letters or special characters)");
            }
        }
    }
}
