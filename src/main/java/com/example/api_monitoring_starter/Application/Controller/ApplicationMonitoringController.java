package com.example.api_monitoring_starter.Application.Controller;

import com.example.api_monitoring_starter.Application.DTO.ApplicationInfoDTO;
import com.example.api_monitoring_starter.Application.Service.ApplicationMonitoringService;
import com.example.api_monitoring_starter.Application.Logging.MemoryLogAppender;

import ch.qos.logback.classic.spi.ILoggingEvent;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/monitoring/applications")
public class ApplicationMonitoringController {

    private final ApplicationMonitoringService service;

    public ApplicationMonitoringController(
            ApplicationMonitoringService service
    ) {
        this.service = service;
    }

    // =========================================================
    // APPLICATION INFO
    // =========================================================

    @GetMapping("/info")
    public ApplicationInfoDTO application() {

        return service.getApplicationInfo();
    }

    // =========================================================
    // CURRENT LOGS
    // =========================================================

    @GetMapping("/logs")
    public List<LogDTO> logs() {

        return MemoryLogAppender.getLogs()
                .stream()
                .map(this::convertLog)
                .toList();
    }

    // =========================================================
    // LIVE LOG STREAM
    // =========================================================

    @GetMapping(
            value = "/logs/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter streamLogs() {

        SseEmitter emitter = new SseEmitter(0L);

        ScheduledExecutorService executor =
                Executors.newSingleThreadScheduledExecutor();

        final int[] lastLogCount = {MemoryLogAppender.getLogs().size()};

        executor.scheduleAtFixedRate(() -> {

            try {

                List<ILoggingEvent> logs =
                        MemoryLogAppender.getLogs();

                // Send only new logs
                if (logs.size() > lastLogCount[0]) {

                    for (int i = lastLogCount[0]; i < logs.size(); i++) {

                        LogDTO log =
                                convertLog(logs.get(i));

                        emitter.send(
                                SseEmitter.event()
                                        .name("log")
                                        .data(log)
                        );
                    }

                    lastLogCount[0] = logs.size();
                }

                // Keep SSE connection alive
                emitter.send(
                        SseEmitter.event()
                                .comment("heartbeat")
                );

            } catch (IOException e) {

                executor.shutdown();
                emitter.complete();

            } catch (Exception e) {

                executor.shutdown();
                emitter.completeWithError(e);
            }

        }, 0, 1, TimeUnit.SECONDS);

        emitter.onCompletion(executor::shutdown);
        emitter.onTimeout(() -> {
            executor.shutdown();
            emitter.complete();
        });

        emitter.onError(error -> executor.shutdown());

        return emitter;
    }

    // =========================================================
    // CONVERT LOGBACK EVENT
    // =========================================================

    private LogDTO convertLog(ILoggingEvent event) {

        return new LogDTO(
                event.getLevel().toString(),
                event.getFormattedMessage(),
                event.getLoggerName(),
                event.getThreadName(),
                event.getTimeStamp()
        );
    }

    // =========================================================
    // LOG DTO
    // =========================================================

    public record LogDTO(
            String level,
            String message,
            String logger,
            String thread,
            long timestamp
    ) {
    }
}