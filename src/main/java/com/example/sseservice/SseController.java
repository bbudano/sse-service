package com.example.sseservice;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping(path = "/sse")
@RequiredArgsConstructor
public class SseController {

    private final SseHandler sseHandler;

    @GetMapping
    public SseEmitter register(@RequestParam String user) {
        return sseHandler.register(user);
    }

}
