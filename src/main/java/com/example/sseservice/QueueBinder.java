package com.example.sseservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueBinder {

    ConcurrentHashMap<String, Binding> bindingMap = new ConcurrentHashMap<>();

    private final DirectExchange directExchange;
    private final Queue queue;
    private final RabbitAdmin rabbitAdmin;

    public void bind(String routingKey) {
        log.info("Binding queue {} to exchange {} with routing key {}", queue.getName(),
                directExchange.getName(), routingKey);
        var binding = BindingBuilder
                .bind(queue)
                .to(directExchange)
                .with(routingKey);
        rabbitAdmin.declareBinding(binding);
        bindingMap.put(routingKey, binding);
    }

    public void unbind(String routingKey) {
        log.info("Removing binding of queue {} to exchange {} with routing key {}", queue.getName(),
                directExchange.getName(), routingKey);
        if (bindingMap.containsKey(routingKey)) {
            rabbitAdmin.removeBinding(bindingMap.get(routingKey));
            bindingMap.remove(routingKey);
        }
    }

}
