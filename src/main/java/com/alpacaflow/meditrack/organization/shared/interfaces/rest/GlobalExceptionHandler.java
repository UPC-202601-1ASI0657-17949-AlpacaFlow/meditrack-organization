package com.alpacaflow.meditrack.organization.shared.interfaces.rest;

import com.alpacaflow.meditrack.organization.admin.domain.exceptions.AdminAlreadyExistsForUserException;
import com.alpacaflow.meditrack.organization.admin.domain.exceptions.AdminInvalidRoleException;
import com.alpacaflow.meditrack.organization.admin.domain.exceptions.AdminNotFoundException;
import com.alpacaflow.meditrack.organization.caregiver.domain.exceptions.CaregiverDuplicateRegistrationException;
import com.alpacaflow.meditrack.organization.caregiver.domain.exceptions.CaregiverInvalidRoleException;
import com.alpacaflow.meditrack.organization.caregiver.domain.exceptions.CaregiverNotFoundException;
import com.alpacaflow.meditrack.organization.doctor.domain.exceptions.DoctorDuplicateRegistrationException;
import com.alpacaflow.meditrack.organization.doctor.domain.exceptions.DoctorInvalidRoleException;
import com.alpacaflow.meditrack.organization.doctor.domain.exceptions.DoctorNotFoundException;
import com.alpacaflow.meditrack.organization.organization.domain.exceptions.OrganizationNotFoundException;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.exceptions.DeviceUnavailableException;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.exceptions.SeniorCitizenAssignmentException;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.exceptions.SeniorCitizenDuplicateRegistrationException;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.exceptions.SeniorCitizenNotFoundException;
import com.alpacaflow.meditrack.organization.shared.interfaces.rest.errors.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        String message = ex.getFieldErrors().stream().map(fieldError -> fieldError.getDefaultMessage() == null ? "" : fieldError.getDefaultMessage()).reduce("", String::concat);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request);
    }

    @ExceptionHandler(OrganizationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrganizationNotFound(OrganizationNotFoundException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(AdminNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAdminNotFound(AdminNotFoundException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(AdminAlreadyExistsForUserException.class)
    public ResponseEntity<ErrorResponse> handleAdminAlreadyExists(AdminAlreadyExistsForUserException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage(), request);
    }

    @ExceptionHandler(AdminInvalidRoleException.class)
    public ResponseEntity<ErrorResponse> handleAdminInvalidRole(AdminInvalidRoleException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_ROLE", ex.getMessage(), request);
    }

    @ExceptionHandler(DoctorNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDoctorNotFound(DoctorNotFoundException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(DoctorDuplicateRegistrationException.class)
    public ResponseEntity<ErrorResponse> handleDoctorDuplicate(DoctorDuplicateRegistrationException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(DoctorInvalidRoleException.class)
    public ResponseEntity<ErrorResponse> handleDoctorInvalidRole(DoctorInvalidRoleException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_ROLE", ex.getMessage(), request);
    }

    @ExceptionHandler(CaregiverNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCaregiverNotFound(CaregiverNotFoundException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(CaregiverDuplicateRegistrationException.class)
    public ResponseEntity<ErrorResponse> handleCaregiverDuplicate(CaregiverDuplicateRegistrationException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(CaregiverInvalidRoleException.class)
    public ResponseEntity<ErrorResponse> handleCaregiverInvalidRole(CaregiverInvalidRoleException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_ROLE", ex.getMessage(), request);
    }

    @ExceptionHandler(SeniorCitizenNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSeniorCitizenNotFound(SeniorCitizenNotFoundException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(SeniorCitizenDuplicateRegistrationException.class)
    public ResponseEntity<ErrorResponse> handleSeniorCitizenDuplicate(SeniorCitizenDuplicateRegistrationException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(SeniorCitizenAssignmentException.class)
    public ResponseEntity<ErrorResponse> handleSeniorCitizenAssignment(SeniorCitizenAssignmentException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(DeviceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleDeviceUnavailable(DeviceUnavailableException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, "DEVICES_UNAVAILABLE", ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", ex.getMessage(), request);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String code, String message, WebRequest request) {
        String path = request instanceof ServletWebRequest servletWebRequest
                ? servletWebRequest.getRequest().getRequestURI()
                : "";
        return ResponseEntity.status(status).body(ErrorResponse.of(status.value(), code, message, path));
    }
}
