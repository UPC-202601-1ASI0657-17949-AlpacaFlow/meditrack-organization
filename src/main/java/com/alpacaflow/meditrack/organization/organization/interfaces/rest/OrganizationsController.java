package com.alpacaflow.meditrack.organization.organization.interfaces.rest;

import com.alpacaflow.meditrack.organization.organization.domain.model.commands.DeleteOrganizationCommand;
import com.alpacaflow.meditrack.organization.organization.domain.model.queries.GetAllOrganizationsQuery;
import com.alpacaflow.meditrack.organization.organization.domain.model.queries.GetOrganizationByIdQuery;
import com.alpacaflow.meditrack.organization.organization.domain.services.OrganizationCommandService;
import com.alpacaflow.meditrack.organization.organization.domain.services.OrganizationQueryService;
import com.alpacaflow.meditrack.organization.organization.interfaces.rest.resources.request.CreateOrganizationRequest;
import com.alpacaflow.meditrack.organization.organization.interfaces.rest.resources.request.UpdateOrganizationRequest;
import com.alpacaflow.meditrack.organization.organization.interfaces.rest.resources.response.OrganizationResponse;
import com.alpacaflow.meditrack.organization.organization.interfaces.rest.transform.CreateOrganizationCommandFromRequestAssembler;
import com.alpacaflow.meditrack.organization.organization.interfaces.rest.transform.OrganizationResponseFromEntityAssembler;
import com.alpacaflow.meditrack.organization.organization.interfaces.rest.transform.UpdateOrganizationCommandFromRequestAssembler;
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
@RequestMapping(value = "/api/v1/organizations", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Organizations", description = "Organization bounded context — CRUD API")
public class OrganizationsController {

    private final OrganizationCommandService organizationCommandService;
    private final OrganizationQueryService organizationQueryService;

    public OrganizationsController(
            OrganizationCommandService organizationCommandService,
            OrganizationQueryService organizationQueryService) {
        this.organizationCommandService = organizationCommandService;
        this.organizationQueryService = organizationQueryService;
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Create organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Duplicate name or email")
    })
    public ResponseEntity<OrganizationResponse> createOrganization(@Valid @RequestBody CreateOrganizationRequest request) {
        var command = CreateOrganizationCommandFromRequestAssembler.toCommand(request);
        var id = organizationCommandService.handle(command);
        var organization = organizationQueryService.handle(new GetOrganizationByIdQuery(id)).orElseThrow();
        return new ResponseEntity<>(OrganizationResponseFromEntityAssembler.toResponse(organization), HttpStatus.CREATED);
    }

    @GetMapping("/{organizationId}")
    @Operation(summary = "Get organization by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<OrganizationResponse> getOrganizationById(@PathVariable Long organizationId) {
        return organizationQueryService.handle(new GetOrganizationByIdQuery(organizationId))
                .map(OrganizationResponseFromEntityAssembler::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List all organizations")
    @ApiResponse(responseCode = "200", description = "List (unpaginated in sprint 1)")
    public ResponseEntity<List<OrganizationResponse>> getAllOrganizations() {
        var organizations = organizationQueryService.handle(new GetAllOrganizationsQuery());
        var body = organizations.stream()
                .map(OrganizationResponseFromEntityAssembler::toResponse)
                .toList();
        return ResponseEntity.ok(body);
    }

    @PutMapping(path = "/{organizationId}", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Update organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate name or email")
    })
    public ResponseEntity<OrganizationResponse> updateOrganization(
            @PathVariable Long organizationId,
            @Valid @RequestBody UpdateOrganizationRequest request) {
        var command = UpdateOrganizationCommandFromRequestAssembler.toCommand(organizationId, request);
        return organizationCommandService.handle(command)
                .map(OrganizationResponseFromEntityAssembler::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{organizationId}")
    @Operation(summary = "Delete organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> deleteOrganization(@PathVariable Long organizationId) {
        organizationCommandService.handle(new DeleteOrganizationCommand(organizationId));
        return ResponseEntity.noContent().build();
    }
}
