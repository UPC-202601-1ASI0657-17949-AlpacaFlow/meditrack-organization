package com.alpacaflow.meditrack.organization.shared.infrastructure.acl.client;

public record RemoteRegisterDeviceRequest(Long deviceId, Long holderId, String model) {
}
