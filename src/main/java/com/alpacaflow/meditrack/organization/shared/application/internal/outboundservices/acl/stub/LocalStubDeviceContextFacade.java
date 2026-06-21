package com.alpacaflow.meditrack.organization.shared.application.internal.outboundservices.acl.stub;

import com.alpacaflow.meditrack.organization.shared.application.internal.outboundservices.acl.DeviceContextFacade;
import com.alpacaflow.meditrack.organization.shared.application.internal.outboundservices.acl.ExternalDevice;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Local, in-memory stub for the Devices bounded context.
 * <p>
 * Active when {@code app.devices.adapter=stub} (default). Allows the organization
 * microservice to run and be tested without depending on the real Devices service.
 * Replacing this with a REST adapter is a configuration switch, not a code change.
 */
@Component
@ConditionalOnProperty(name = "app.devices.adapter", havingValue = "stub", matchIfMissing = true)
public class LocalStubDeviceContextFacade implements DeviceContextFacade {

    private static final String DEFAULT_STATUS = "available";

    private final ConcurrentHashMap<Long, ExternalDevice> devicesById = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(5000);

    @Override
    public boolean deviceExists(Long deviceId) {
        if (deviceId == null || deviceId <= 0) {
            return false;
        }
        return devicesById.containsKey(deviceId);
    }

    @Override
    public Optional<ExternalDevice> findDeviceById(Long deviceId) {
        if (deviceId == null || deviceId <= 0) {
            return Optional.empty();
        }
        return Optional.ofNullable(devicesById.get(deviceId));
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
        if (seniorCitizenId != null && seniorCitizenId > 0) {
            devicesById.put(deviceId, new ExternalDevice(deviceId, DEFAULT_STATUS));
            return;
        }
        devicesById.putIfAbsent(deviceId, new ExternalDevice(deviceId, DEFAULT_STATUS));
    }
}
