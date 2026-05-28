package com.alpacaflow.meditrack.organization.caregiver.interfaces.rest;

import com.alpacaflow.meditrack.organization.caregiver.domain.model.commands.DeleteCaregiverCommand;
import com.alpacaflow.meditrack.organization.caregiver.domain.model.queries.GetAllCaregiversByOrganizationIdQuery;
import com.alpacaflow.meditrack.organization.caregiver.domain.model.queries.GetAllCaregiversQuery;
import com.alpacaflow.meditrack.organization.caregiver.domain.model.queries.GetCaregiverByIdQuery;
import com.alpacaflow.meditrack.organization.caregiver.domain.model.queries.GetCaregiverByUserIdAndOrganizationIdQuery;
import com.alpacaflow.meditrack.organization.caregiver.domain.model.queries.GetCaregiverByUserIdQuery;
import com.alpacaflow.meditrack.organization.caregiver.domain.services.CaregiverCommandService;
import com.alpacaflow.meditrack.organization.caregiver.domain.services.CaregiverQueryService;
import com.alpacaflow.meditrack.organization.caregiver.interfaces.rest.resources.request.CreateCaregiverRequest;
import com.alpacaflow.meditrack.organization.caregiver.interfaces.rest.resources.request.UpdateCaregiverRequest;
import com.alpacaflow.meditrack.organization.caregiver.interfaces.rest.resources.response.CaregiverResponse;
import com.alpacaflow.meditrack.organization.caregiver.interfaces.rest.transform.CaregiverResponseFromEntityAssembler;
import com.alpacaflow.meditrack.organization.caregiver.interfaces.rest.transform.CreateCaregiverCommandFromRequestAssembler;
import com.alpacaflow.meditrack.organization.caregiver.interfaces.rest.transform.UpdateCaregiverCommandFromRequestAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/v1/caregivers", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Caregivers", description = "Caregiver entity within the organization bounded context")
public class CaregiversController {

    private final CaregiverCommandService caregiverCommandService;
    private final CaregiverQueryService caregiverQueryService;

    public CaregiversController(CaregiverCommandService caregiverCommandService,
                                CaregiverQueryService caregiverQueryService) {
        this.caregiverCommandService = caregiverCommandService;
        this.caregiverQueryService = caregiverQueryService;
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Create caregiver")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Organization or user not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate caregiver (email or full name) within organization")
    })
    public ResponseEntity<CaregiverResponse> createCaregiver(@Valid @RequestBody CreateCaregiverRequest request) {
        var command = CreateCaregiverCommandFromRequestAssembler.toCommand(request);
        var id = caregiverCommandService.handle(command);
        var caregiver = caregiverQueryService.handle(new GetCaregiverByIdQuery(id)).orElseThrow();
        return new ResponseEntity<>(CaregiverResponseFromEntityAssembler.toResponse(caregiver), HttpStatus.CREATED);
    }

    @GetMapping("/{caregiverId}")
    @Operation(summary = "Get caregiver by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<CaregiverResponse> getCaregiverById(@PathVariable Long caregiverId) {
        return caregiverQueryService.handle(new GetCaregiverByIdQuery(caregiverId))
                .map(CaregiverResponseFromEntityAssembler::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List all caregivers")
    public ResponseEntity<List<CaregiverResponse>> getAllCaregivers() {
        var caregivers = caregiverQueryService.handle(new GetAllCaregiversQuery());
        var body = caregivers.stream()
                .map(CaregiverResponseFromEntityAssembler::toResponse)
                .toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "List caregivers by organization")
    public ResponseEntity<List<CaregiverResponse>> getCaregiversByOrganization(@PathVariable Long organizationId) {
        var caregivers = caregiverQueryService.handle(new GetAllCaregiversByOrganizationIdQuery(organizationId));
        var body = caregivers.stream()
                .map(CaregiverResponseFromEntityAssembler::toResponse)
                .toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get caregiver by IAM user id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<CaregiverResponse> getCaregiverByUserId(@PathVariable Long userId) {
        return caregiverQueryService.handle(new GetCaregiverByUserIdQuery(userId))
                .map(CaregiverResponseFromEntityAssembler::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}/organization/{organizationId}")
    @Operation(summary = "Get caregiver by user and organization (multi-tenant lookup)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<CaregiverResponse> getCaregiverByUserIdAndOrganizationId(@PathVariable Long userId,
                                                                                    @PathVariable Long organizationId) {
        return caregiverQueryService.handle(new GetCaregiverByUserIdAndOrganizationIdQuery(userId, organizationId))
                .map(CaregiverResponseFromEntityAssembler::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping(path = "/{caregiverId}", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Update caregiver")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate caregiver in organization")
    })
    public ResponseEntity<CaregiverResponse> updateCaregiver(@PathVariable Long caregiverId,
                                                             @Valid @RequestBody UpdateCaregiverRequest request) {
        var command = UpdateCaregiverCommandFromRequestAssembler.toCommand(caregiverId, request);
        return caregiverCommandService.handle(command)
                .map(CaregiverResponseFromEntityAssembler::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{caregiverId}")
    @Operation(summary = "Delete caregiver")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> deleteCaregiver(@PathVariable Long caregiverId) {
        caregiverCommandService.handle(new DeleteCaregiverCommand(caregiverId));
        return ResponseEntity.noContent().build();
    }
}
