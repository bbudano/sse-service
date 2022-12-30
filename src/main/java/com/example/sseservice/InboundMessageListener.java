package com.example.sseservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class InboundMessageListener {

    private final SseHandler sseHandler;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMqConfig.QUEUE_NAME)
    public void handleMessage(Message message) {
        try {
            var data = SseEmitter
                    .event()
                    .id(UUID.randomUUID().toString())
                    .name("amqp-message")
                    .data(objectMapper.readValue(message.getBody(), Object.class))
                    .reconnectTime(2_000L);
            sseHandler.emit(data, message.getMessageProperties().getReceivedRoutingKey());
        } catch (IOException e) {
            log.error("Could not read Message body:", e);
        }
    }

}
