package com.restaurant.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilita el broker de mensajes para enviar notificaciones a los clientes
        // en destinos con el prefijo /topic
        config.enableSimpleBroker("/topic");
        // Prefijo para mensajes que el cliente envía al servidor
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // El cliente (React) se conectará a esta ruta
        registry.addEndpoint("/ws/alerts")
                .setAllowedOriginPatterns("*") // Permite CORS temporalmente
                .withSockJS();
    }
}
