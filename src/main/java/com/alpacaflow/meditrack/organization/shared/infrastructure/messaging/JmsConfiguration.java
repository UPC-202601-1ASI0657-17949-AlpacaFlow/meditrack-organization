package com.alpacaflow.meditrack.organization.shared.infrastructure.messaging;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.ConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJms
@ConditionalOnProperty(name = "app.messaging.enabled", havingValue = "true")
public class JmsConfiguration {

    private static final String LEGACY_IAM_ADMIN_REGISTRATION_TYPE =
            "com.alpacaflow.meditrack.iam.shared.infrastructure.messaging.AdminRegistrationRequestedMessage";
    private static final String LEGACY_IAM_STAFF_PROVISION_RESPONSE_TYPE =
            "com.alpacaflow.meditrack.iam.shared.infrastructure.messaging.StaffProvisionResponseMessage";

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        var objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        var converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");

        Map<String, Class<?>> typeIdMappings = new HashMap<>();
        typeIdMappings.put(MessagingTypeIds.ADMIN_REGISTRATION_REQUEST, AdminRegistrationRequestedMessage.class);
        typeIdMappings.put(MessagingTypeIds.STAFF_PROVISION_REQUEST, StaffProvisionRequestMessage.class);
        typeIdMappings.put(MessagingTypeIds.STAFF_PROVISION_RESPONSE, StaffProvisionResponseMessage.class);
        typeIdMappings.put(LEGACY_IAM_ADMIN_REGISTRATION_TYPE, AdminRegistrationRequestedMessage.class);
        typeIdMappings.put(LEGACY_IAM_STAFF_PROVISION_RESPONSE_TYPE, StaffProvisionResponseMessage.class);
        converter.setTypeIdMappings(typeIdMappings);
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
    public JmsTemplate jmsTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter jacksonJmsMessageConverter) {
        var template = new JmsTemplate(connectionFactory);
        template.setMessageConverter(jacksonJmsMessageConverter);
        template.setReceiveTimeout(12_000);
        return template;
    }
}
