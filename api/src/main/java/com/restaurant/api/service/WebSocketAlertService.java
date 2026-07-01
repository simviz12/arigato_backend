package com.restaurant.api.service;

import com.restaurant.domain.port.AlertPort;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class WebSocketAlertService implements AlertPort {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendInventoryAlert(String message) {
        messagingTemplate.convertAndSend("/topic/inventory-alerts", Map.of("message", message));
    }
}
