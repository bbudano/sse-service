package com.example.sseservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseHandler {

    Map<String, ConcurrentHashMap<String, SseEmitter>> userEmittersMap = new ConcurrentHashMap<>();

    private final QueueBinder queueBinder;

    public SseEmitter register(String user) {
        var newEmitterId = UUID.randomUUID().toString();

        var newEmitter = this.createEmitter(user, newEmitterId, 24 * 60 * 60 * 1_000L);

        if (userEmittersMap.containsKey(user)) {
            userEmittersMap.get(user).put(newEmitterId, newEmitter);
        } else {
            var emittersMap = new ConcurrentHashMap<String, SseEmitter>();
            emittersMap.put(newEmitterId, newEmitter);

            userEmittersMap.put(user, emittersMap);

            queueBinder.bind(user);
        }

        return newEmitter;
    }

    public void remove(String user, String emitterId) {
        if (userEmittersMap.containsKey(user)) {
            var emittersMap = userEmittersMap.get(user);
            emittersMap.remove(emitterId);

            if (emittersMap.isEmpty()) {
                userEmittersMap.remove(user);
                queueBinder.unbind(user);
            }
        }
    }

    public void emit(SseEmitter.SseEventBuilder sseEventBuilder, String user) {
        if (userEmittersMap.containsKey(user)) {
            var emittersMap = userEmittersMap.get(user);

            emittersMap.forEach((id, emitter) -> {
                try {
                    log.debug("Sending data to emitter {}: {}", id, sseEventBuilder);
                    emitter.send(sseEventBuilder);
                } catch (IOException e) {
                    log.error("Failed to send data - Removing emitter {}", id);
                    this.remove(user, id);
                }
            });
        }
    }

    public void emit(SseEmitter.SseEventBuilder sseEventBuilder) {
        userEmittersMap.forEach((user, emittersMap) ->  emit(sseEventBuilder, user));
    }

    private SseEmitter createEmitter(String user, String id, Long timeout) {
        var sseEmitter = new SseEmitter(timeout);

        sseEmitter.onCompletion(() -> {
            log.info("Emitter {} completed", id);
            remove(user, id);
        });
        sseEmitter.onTimeout(() -> {
            log.info("Emitter {} timed out", id);
            remove(user, id);
        });
        sseEmitter.onError(error -> {
            log.error("Error occurred on emitter {}: ", id, error);
            remove(user, id);
        });

        return sseEmitter;
    }

}
