package com.example.sseservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HeartbeatService {

    private final SseHandler sseHandler;

    @Scheduled(initialDelay = 5_000L, fixedRate = 10_000L)
    public void beat() {
        log.debug("Emitting heartbeat");
        sseHandler.emit(SseEmitter
                .event()
                .id(UUID.randomUUID().toString())
                .name("heartbeat-event")
                .data("beat@" + Instant.now().getEpochSecond())
                .reconnectTime(2_000L));
    }

}
