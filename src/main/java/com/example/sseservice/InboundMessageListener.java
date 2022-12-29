package com.example.sseservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InboundMessageListener {

    @RabbitListener(queues = RabbitMqConfig.QUEUE_NAME)
    public void handleMessage(Message message) {
        log.info("Recieved message: {}", message);
    }

}
