package com.example.api_monitoring_starter.Application.Controller;

import com.example.api_monitoring_starter.Application.Logging.MemoryLogAppender;

import ch.qos.logback.classic.spi.ILoggingEvent;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/monitoring/application/logs")
public class ApplicationLogController {


    @GetMapping
    public List<ILoggingEvent> getLogs() {

        return MemoryLogAppender.getLogs();

    }


    @GetMapping(
            value = "/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter streamLogs() {

        SseEmitter emitter =
                new SseEmitter(0L);

        MemoryLogAppender.addEmitter(emitter);

        return emitter;
    }

}