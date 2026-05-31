package com.alpacaflow.meditrack.organization.shared.infrastructure.messaging;

import jakarta.jms.ConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.Map;

@Configuration
@EnableJms
@ConditionalOnProperty(name = "app.messaging.enabled", havingValue = "true")
public class JmsConfiguration {

    private static final String IAM_ADMIN_REGISTRATION_TYPE =
            "com.alpacaflow.meditrack.iam.shared.infrastructure.messaging.AdminRegistrationRequestedMessage";
    private static final String IAM_STAFF_PROVISION_RESPONSE_TYPE =
            "com.alpacaflow.meditrack.iam.shared.infrastructure.messaging.StaffProvisionResponseMessage";

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        var converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        converter.setTypeIdMappings(Map.of(
                IAM_ADMIN_REGISTRATION_TYPE, AdminRegistrationRequestedMessage.class,
                IAM_STAFF_PROVISION_RESPONSE_TYPE, StaffProvisionResponseMessage.class));
        return converter;
    }

    @Bean
    public JmsListenerContainerFactory<?> jmsListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jacksonJmsMessageConverter) {
        var factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jacksonJmsMessageConverter);
        return factory;
    }

    @Bean
    public JmsMessagingTemplate jmsMessagingTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter jacksonJmsMessageConverter) {
        var template = new JmsMessagingTemplate(connectionFactory);
        template.setJmsMessageConverter(jacksonJmsMessageConverter);
        return template;
    }
}
