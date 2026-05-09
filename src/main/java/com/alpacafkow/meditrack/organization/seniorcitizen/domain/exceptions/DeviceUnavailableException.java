package com.alpacafkow.meditrack.organization.seniorcitizen.domain.exceptions;

/**
 * Thrown by the SeniorCitizen application service when the Devices bounded context
 * cannot satisfy a device creation/lookup request. Translated to HTTP 503 by the
 * global exception handler so callers know the issue is on a downstream service.
 */
public class DeviceUnavailableException extends RuntimeException {
    public DeviceUnavailableException(String message) {
        super(message);
    }
}
