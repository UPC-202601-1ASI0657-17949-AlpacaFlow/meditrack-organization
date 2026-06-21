package com.alpacaflow.meditrack.organization.shared.application.internal.outboundservices.acl.rest;

import com.alpacaflow.meditrack.organization.shared.application.internal.outboundservices.acl.DeviceContextFacade;
import com.alpacaflow.meditrack.organization.shared.application.internal.outboundservices.acl.ExternalDevice;
import com.alpacaflow.meditrack.organization.shared.infrastructure.acl.client.RemoteRegisterDeviceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * REST adapter for {@link DeviceContextFacade}. Calls the Devices microservice.
 */
@Component
@ConditionalOnProperty(name = "app.devices.adapter", havingValue = "rest")
public class RemoteDeviceContextFacade implements DeviceContextFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteDeviceContextFacade.class);

    private final RestClient restClient;
    private final AtomicLong sequence = new AtomicLong(9000);

    public RemoteDeviceContextFacade(@Value("${integration.devices.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl)
                .build();
    }

    @Override
    public boolean deviceExists(Long deviceId) {
        if (deviceId == null || deviceId <= 0) {
            return false;
        }
        try {
            restClient.get()
                    .uri("/api/v1/devices/{deviceId}", deviceId)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (Exception e) {
            LOGGER.error("Failed to check device {} existence: {}", deviceId, e.getMessage());
            return false;
        }
    }

    @Override
    public Optional<ExternalDevice> findDeviceById(Long deviceId) {
        if (!deviceExists(deviceId)) {
            return Optional.empty();
        }
        return Optional.of(new ExternalDevice(deviceId, "ACTIVE"));
    }

    @Override
    public Long createDeviceForSeniorCitizen() {
        long id = reserveNextDeviceId();
        registerDeviceForSeniorCitizen(id, null);
        return id;
    }

    @Override
    public Long reserveNextDeviceId() {
        return sequence.incrementAndGet();
    }

    @Override
    public void registerDeviceForSeniorCitizen(Long deviceId, Long seniorCitizenId) {
        if (deviceId == null || deviceId <= 0) {
            throw new IllegalArgumentException("Invalid device id");
        }
        if (seniorCitizenId == null || seniorCitizenId <= 0) {
            LOGGER.warn("Skipping device registration for device {}: missing seniorCitizenId", deviceId);
            return;
        }
        try {
            restClient.post()
                    .uri("/api/v1/devices/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new RemoteRegisterDeviceRequest(deviceId, seniorCitizenId, "IoT Band"))
                    .retrieve()
                    .toBodilessEntity();
            LOGGER.info("Registered device {} for senior citizen {}", deviceId, seniorCitizenId);
        } catch (Exception e) {
            LOGGER.error("Failed to register device {} for senior citizen {}: {}", deviceId, seniorCitizenId, e.getMessage());
            throw new IllegalStateException("Failed to register device in Devices context: " + e.getMessage(), e);
        }
    }
}
