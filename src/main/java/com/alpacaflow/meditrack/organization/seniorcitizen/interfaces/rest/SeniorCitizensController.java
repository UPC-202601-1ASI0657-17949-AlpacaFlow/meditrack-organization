package com.alpacaflow.meditrack.organization.seniorcitizen.interfaces.rest;

import com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.commands.AssignSeniorCitizenToCaregiverCommand;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.commands.AssignSeniorCitizenToDoctorCommand;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.commands.DeleteSeniorCitizenCommand;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.commands.UnassignSeniorCitizenFromCaregiverCommand;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.commands.UnassignSeniorCitizenFromDoctorCommand;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.queries.GetAllSeniorCitizensByOrganizationIdQuery;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.queries.GetAllSeniorCitizensQuery;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.queries.GetSeniorCitizenByIdQuery;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.queries.GetSeniorCitizensByAssignedCaregiverIdQuery;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.queries.GetSeniorCitizensByAssignedDoctorIdQuery;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.services.SeniorCitizenCommandService;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.services.SeniorCitizenQueryService;
import com.alpacaflow.meditrack.organization.seniorcitizen.interfaces.rest.resources.request.CreateSeniorCitizenRequest;
import com.alpacaflow.meditrack.organization.seniorcitizen.interfaces.rest.resources.request.UpdateSeniorCitizenRequest;
import com.alpacaflow.meditrack.organization.seniorcitizen.interfaces.rest.resources.response.SeniorCitizenResponse;
import com.alpacaflow.meditrack.organization.seniorcitizen.interfaces.rest.transform.CreateSeniorCitizenCommandFromRequestAssembler;
import com.alpacaflow.meditrack.organization.seniorcitizen.interfaces.rest.transform.SeniorCitizenResponseFromEntityAssembler;
import com.alpacaflow.meditrack.organization.seniorcitizen.interfaces.rest.transform.UpdateSeniorCitizenCommandFromRequestAssembler;
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
@RequestMapping(value = "/api/v1/senior-citizens", produces = APPLICATION_JSON_VALUE)
@Tag(name = "SeniorCitizens", description = "Senior citizen entity within the organization bounded context")
public class SeniorCitizensController {

    private final SeniorCitizenCommandService commandService;
    private final SeniorCitizenQueryService queryService;

    public SeniorCitizensController(SeniorCitizenCommandService commandService,
                                    SeniorCitizenQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    // ---------- CRUD ----------

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Create senior citizen")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Organization not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate (DNI / full name) or device already linked"),
            @ApiResponse(responseCode = "503", description = "Devices context unavailable")
    })
    public ResponseEntity<SeniorCitizenResponse> createSeniorCitizen(
            @Valid @RequestBody CreateSeniorCitizenRequest request) {
        var command = CreateSeniorCitizenCommandFromRequestAssembler.toCommand(request);
        var id = commandService.handle(command);
        var seniorCitizen = queryService.handle(new GetSeniorCitizenByIdQuery(id)).orElseThrow();
        return new ResponseEntity<>(
                SeniorCitizenResponseFromEntityAssembler.toResponse(seniorCitizen), HttpStatus.CREATED);
    }

    @GetMapping("/{seniorCitizenId}")
    @Operation(summary = "Get senior citizen by id")
    public ResponseEntity<SeniorCitizenResponse> getSeniorCitizenById(@PathVariable Long seniorCitizenId) {
        return queryService.handle(new GetSeniorCitizenByIdQuery(seniorCitizenId))
                .map(SeniorCitizenResponseFromEntityAssembler::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List all senior citizens")
    public ResponseEntity<List<SeniorCitizenResponse>> getAllSeniorCitizens() {
        var seniors = queryService.handle(new GetAllSeniorCitizensQuery());
        var body = seniors.stream()
                .map(SeniorCitizenResponseFromEntityAssembler::toResponse)
                .toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "List senior citizens by organization")
    public ResponseEntity<List<SeniorCitizenResponse>> getSeniorCitizensByOrganization(
            @PathVariable Long organizationId) {
        var seniors = queryService.handle(new GetAllSeniorCitizensByOrganizationIdQuery(organizationId));
        var body = seniors.stream()
                .map(SeniorCitizenResponseFromEntityAssembler::toResponse)
                .toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "List senior citizens assigned to a doctor")
    public ResponseEntity<List<SeniorCitizenResponse>> getSeniorCitizensAssignedToDoctor(
            @PathVariable Long doctorId) {
        var seniors = queryService.handle(new GetSeniorCitizensByAssignedDoctorIdQuery(doctorId));
        var body = seniors.stream()
                .map(SeniorCitizenResponseFromEntityAssembler::toResponse)
                .toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/caregiver/{caregiverId}")
    @Operation(summary = "List senior citizens assigned to a caregiver")
    public ResponseEntity<List<SeniorCitizenResponse>> getSeniorCitizensAssignedToCaregiver(
            @PathVariable Long caregiverId) {
        var seniors = queryService.handle(new GetSeniorCitizensByAssignedCaregiverIdQuery(caregiverId));
        var body = seniors.stream()
                .map(SeniorCitizenResponseFromEntityAssembler::toResponse)
                .toList();
        return ResponseEntity.ok(body);
    }

    @PutMapping(path = "/{seniorCitizenId}", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Update senior citizen")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate (DNI / full name) or device already linked"),
            @ApiResponse(responseCode = "503", description = "Devices context unavailable")
    })
    public ResponseEntity<SeniorCitizenResponse> updateSeniorCitizen(
            @PathVariable Long seniorCitizenId,
            @Valid @RequestBody UpdateSeniorCitizenRequest request) {
        var command = UpdateSeniorCitizenCommandFromRequestAssembler.toCommand(seniorCitizenId, request);
        return commandService.handle(command)
                .map(SeniorCitizenResponseFromEntityAssembler::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{seniorCitizenId}")
    @Operation(summary = "Delete senior citizen")
    public ResponseEntity<Void> deleteSeniorCitizen(@PathVariable Long seniorCitizenId) {
        commandService.handle(new DeleteSeniorCitizenCommand(seniorCitizenId));
        return ResponseEntity.noContent().build();
    }

    // ---------- Assignment endpoints ----------

    @PostMapping("/{seniorCitizenId}/assignments/doctor/{doctorId}")
    @Operation(summary = "Assign senior citizen to doctor",
            description = "Mutual exclusion: a senior citizen can be assigned to a doctor OR a caregiver, never both.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assigned"),
            @ApiResponse(responseCode = "404", description = "Senior citizen or doctor not found"),
            @ApiResponse(responseCode = "409", description = "Already assigned to a caregiver, or different organization")
    })
    public ResponseEntity<SeniorCitizenResponse> assignSeniorCitizenToDoctor(
            @PathVariable Long seniorCitizenId,
            @PathVariable Long doctorId) {
        return commandService.handle(new AssignSeniorCitizenToDoctorCommand(seniorCitizenId, doctorId))
                .map(SeniorCitizenResponseFromEntityAssembler::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{seniorCitizenId}/assignments/doctor/{doctorId}")
    @Operation(summary = "Unassign senior citizen from doctor")
    public ResponseEntity<SeniorCitizenResponse> unassignSeniorCitizenFromDoctor(
            @PathVariable Long seniorCitizenId,
            @PathVariable Long doctorId) {
        return commandService.handle(new UnassignSeniorCitizenFromDoctorCommand(seniorCitizenId, doctorId))
                .map(SeniorCitizenResponseFromEntityAssembler::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{seniorCitizenId}/assignments/caregiver/{caregiverId}")
    @Operation(summary = "Assign senior citizen to caregiver",
            description = "Mutual exclusion: a senior citizen can be assigned to a doctor OR a caregiver, never both.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assigned"),
            @ApiResponse(responseCode = "404", description = "Senior citizen or caregiver not found"),
            @ApiResponse(responseCode = "409", description = "Already assigned to a doctor, or different organization")
    })
    public ResponseEntity<SeniorCitizenResponse> assignSeniorCitizenToCaregiver(
            @PathVariable Long seniorCitizenId,
            @PathVariable Long caregiverId) {
        return commandService.handle(new AssignSeniorCitizenToCaregiverCommand(seniorCitizenId, caregiverId))
                .map(SeniorCitizenResponseFromEntityAssembler::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{seniorCitizenId}/assignments/caregiver/{caregiverId}")
    @Operation(summary = "Unassign senior citizen from caregiver")
    public ResponseEntity<SeniorCitizenResponse> unassignSeniorCitizenFromCaregiver(
            @PathVariable Long seniorCitizenId,
            @PathVariable Long caregiverId) {
        return commandService.handle(new UnassignSeniorCitizenFromCaregiverCommand(seniorCitizenId, caregiverId))
                .map(SeniorCitizenResponseFromEntityAssembler::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
