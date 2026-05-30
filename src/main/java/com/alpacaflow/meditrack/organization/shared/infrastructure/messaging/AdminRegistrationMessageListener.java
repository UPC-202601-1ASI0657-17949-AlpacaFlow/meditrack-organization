package com.alpacaflow.meditrack.organization.shared.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.messaging.enabled", havingValue = "true")
public class AdminRegistrationMessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminRegistrationMessageListener.class);

    private final AdminRegistrationMessageHandler handler;

    public AdminRegistrationMessageListener(AdminRegistrationMessageHandler handler) {
        this.handler = handler;
    }

    @JmsListener(
            destination = MessagingQueueNames.ADMIN_REGISTRATION_REQUESTED,
            containerFactory = "jmsListenerContainerFactory")
    public void onAdminRegistrationRequested(AdminRegistrationRequestedMessage message) {
        LOGGER.info("Received admin registration message eventId={}", message.eventId());
        handler.handle(message);
    }
}
