package com.alpacaflow.meditrack.organization.organization.interfaces.rest;

import com.alpacaflow.meditrack.organization.organization.domain.model.queries.GetOrganizationByIdQuery;
import com.alpacaflow.meditrack.organization.organization.domain.services.OrganizationCommandService;
import com.alpacaflow.meditrack.organization.organization.domain.services.OrganizationQueryService;
import com.alpacaflow.meditrack.organization.organization.interfaces.rest.resources.request.CreateOrganizationRequest;
import com.alpacaflow.meditrack.organization.organization.interfaces.rest.resources.response.OrganizationResponse;
import com.alpacaflow.meditrack.organization.organization.interfaces.rest.transform.CreateOrganizationCommandFromRequestAssembler;
import com.alpacaflow.meditrack.organization.organization.interfaces.rest.transform.OrganizationResponseFromEntityAssembler;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Server-to-server ACL used by IAM when {@code app.organization.adapter=rest}.
 */
@Hidden
@RestController
@RequestMapping(value = "/api/v1/internal/organizations", produces = APPLICATION_JSON_VALUE)
public class InternalOrganizationsController {

    private final OrganizationCommandService organizationCommandService;
    private final OrganizationQueryService organizationQueryService;

    public InternalOrganizationsController(
            OrganizationCommandService organizationCommandService,
            OrganizationQueryService organizationQueryService) {
        this.organizationCommandService = organizationCommandService;
        this.organizationQueryService = organizationQueryService;
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<OrganizationResponse> createOrganization(@Valid @RequestBody CreateOrganizationRequest request) {
        var command = CreateOrganizationCommandFromRequestAssembler.toCommand(request);
        var id = organizationCommandService.handle(command);
        var organization = organizationQueryService.handle(new GetOrganizationByIdQuery(id)).orElseThrow();
        return new ResponseEntity<>(OrganizationResponseFromEntityAssembler.toResponse(organization), HttpStatus.CREATED);
    }
}
