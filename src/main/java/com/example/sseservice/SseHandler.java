package com.example.sseservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SseHandler {

    Map<String, ConcurrentHashMap<String, SseEmitter>> userEmittersMap = new ConcurrentHashMap<>();

    public SseEmitter register(String user) {
        var newEmitter = new SseEmitter(60 * 1000L);
        var newEmitterId = UUID.randomUUID().toString();

        if (userEmittersMap.containsKey(user)) {
            userEmittersMap.get(user).put(newEmitterId, newEmitter);
        } else {
            var emittersMap = new ConcurrentHashMap<String, SseEmitter>();
            emittersMap.put(newEmitterId, newEmitter);
            userEmittersMap.put(user, emittersMap);
        }

        return newEmitter;
    }

    public void emit(SseEmitter.SseEventBuilder sseEventBuilder) {
        userEmittersMap.forEach((user, emittersMap) -> {
            emittersMap.forEach((id, emitter) -> {
                try {
                    emitter.send(sseEventBuilder);
                } catch (IOException e) {
                    log.error("Failed to send event - Removing emitter {}", id);

                    emittersMap.remove(id);
                    if (emittersMap.isEmpty()) {
                        userEmittersMap.remove(user);
                    }
                }
            });
        });
    }

}
