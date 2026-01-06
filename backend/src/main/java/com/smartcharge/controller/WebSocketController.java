package com.smartcharge.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
public class WebSocketController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/order/status")
    @SendTo("/topic/order")
    public Map<String, Object> getOrderStatus(Map<String, Object> message) {
        Long orderId = Long.valueOf(message.get("orderId").toString());
        
        // 这里应该从数据库获取订单实时状态
        Map<String, Object> response = new HashMap<>();
        response.put("orderId", orderId);
        response.put("status", "charging");
        response.put("currentPower", 15.5);
        response.put("currentPowerRate", 7.5);
        response.put("currentAmount", 25.50);
        
        return response;
    }

    public void sendOrderUpdate(Long orderId, Map<String, Object> data) {
        messagingTemplate.convertAndSend("/topic/order/" + orderId, data);
    }
}


