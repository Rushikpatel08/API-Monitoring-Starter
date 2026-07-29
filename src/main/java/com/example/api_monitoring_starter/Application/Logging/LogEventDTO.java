package com.example.api_monitoring_starter.Application.Logging;

import ch.qos.logback.classic.spi.ILoggingEvent;

import java.time.Instant;

public class LogEventDTO {

    private String level;
    private String message;
    private String logger;
    private String thread;
    private String timestamp;


    public LogEventDTO(ILoggingEvent event) {

        this.level = event.getLevel().toString();

        this.message = event.getFormattedMessage();

        this.logger = event.getLoggerName();

        this.thread = event.getThreadName();

        this.timestamp =
                Instant.ofEpochMilli(
                        event.getTimeStamp()
                ).toString();
    }


    public String getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public String getLogger() {
        return logger;
    }

    public String getThread() {
        return thread;
    }

    public String getTimestamp() {
        return timestamp;
    }
}