package com.monitoring.api_monitoring_starter.Application.Logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MemoryLogAppender extends AppenderBase<ILoggingEvent> {

    private static final int MAX_LOGS = 500;

    private static final List<ILoggingEvent> logs =
            new CopyOnWriteArrayList<>();

    private static final List<SseEmitter> emitters =
            new CopyOnWriteArrayList<>();


    // =========================================================
    // LOG APPENDER
    // =========================================================

    @Override
    protected void append(ILoggingEvent event) {

        logs.add(event);

        // Keep only latest 500 logs
        if (logs.size() > MAX_LOGS) {
            logs.remove(0);
        }

        // Send live log to connected browsers
        broadcast(event);
    }


    // =========================================================
    // ADD SSE CLIENT
    // =========================================================

    public static void addEmitter(SseEmitter emitter) {

        emitters.add(emitter);

        emitter.onCompletion(() ->
                emitters.remove(emitter)
        );

        emitter.onTimeout(() ->
                emitters.remove(emitter)
        );

        emitter.onError(error ->
                emitters.remove(emitter)
        );
    }


    // =========================================================
    // BROADCAST LOG
    // =========================================================

    private static void broadcast(ILoggingEvent event) {

        for (SseEmitter emitter : emitters) {

            try {

                emitter.send(
                        new LogEvent(
                                event.getLevel().toString(),
                                event.getFormattedMessage(),
                                event.getLoggerName(),
                                event.getThreadName(),
                                event.getTimeStamp()
                        )
                );

            }
            catch (IOException e) {

                emitter.complete();

                emitters.remove(emitter);
            }
        }
    }


    // =========================================================
    // GET EXISTING LOGS
    // =========================================================

    public static List<ILoggingEvent> getLogs() {

        return List.copyOf(logs);

    }


    // =========================================================
    // LOG EVENT DTO
    // =========================================================

    public record LogEvent(
            String level,
            String message,
            String logger,
            String thread,
            long timestamp
    ) {
    }

}