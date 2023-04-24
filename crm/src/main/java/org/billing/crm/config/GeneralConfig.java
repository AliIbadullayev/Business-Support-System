package org.billing.crm.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.jms.ConnectionFactory;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

@Configuration
@EnableJms
public class GeneralConfig {
    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(@Qualifier("jmsConnectionFactory") ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
//        factory.setErrorHandler(errorHandler);
        return factory;
    }

    @Bean
    public OpenAPI customize() {
            return new OpenAPI()
                    .info(new Info().title("Billing-Support-System").version("3.0.0"))
                    // Components section defines Security Scheme "mySecretHeader"
                    .components(new Components()
                            .addSecuritySchemes("Authorization", new SecurityScheme()
                                    .type(SecurityScheme.Type.APIKEY)
                                    .in(SecurityScheme.In.HEADER)
                                    .name("Authorization")))
                    // AddSecurityItem section applies created scheme globally
                    .addSecurityItem(new SecurityRequirement().addList("Authorization"));
    }
}
