package com.alpacaflow.meditrack.organization.shared.application.internal.outboundservices.acl;

import java.util.Optional;

/**
 * Port (Anti-Corruption Layer) used by the organization microservice to talk to the
 * Devices bounded context.
 * <p>
 * Implementations may be a local in-memory stub (sprint 1) or a REST client targeting the
 * real Devices microservice (later sprints), without affecting business logic.
 */
public interface DeviceContextFacade {

    /**
     * @return true if a device with the provided id exists in the Devices context.
     */
    boolean deviceExists(Long deviceId);

    /**
     * Looks up a device by id.
     */
    Optional<ExternalDevice> findDeviceById(Long deviceId);

    /**
     * Creates a new device for an unassigned senior citizen.
     * <p>
     * The Devices context owns its own identifier strategy; this method returns the new id.
     *
     * @return the id of the newly created device
     */
    Long createDeviceForSeniorCitizen();

    /**
     * Reserves the next device identifier without registering the device yet.
     * Used to defer device registration until the senior citizen is persisted successfully.
     */
    Long reserveNextDeviceId();

    /**
     * Registers a device in the Devices context after a senior citizen was saved successfully.
     */
    void registerDeviceForSeniorCitizen(Long deviceId);
}
