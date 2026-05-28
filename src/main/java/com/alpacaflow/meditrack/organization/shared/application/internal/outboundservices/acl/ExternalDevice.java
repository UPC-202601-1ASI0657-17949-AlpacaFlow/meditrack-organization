package com.alpacaflow.meditrack.organization.shared.application.internal.outboundservices.acl;

/**
 * Internal representation of a device owned by the (future) Devices bounded context.
 * Used by {@link DeviceContextFacade} so the rest of the organization microservice
 * never depends on Devices domain types directly (Anti-Corruption Layer).
 */
public record ExternalDevice(Long id, String status) {
}
