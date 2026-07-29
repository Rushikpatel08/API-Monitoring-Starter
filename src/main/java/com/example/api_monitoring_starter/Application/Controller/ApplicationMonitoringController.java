package com.example.api_monitoring_starter.Application.Controller;

import com.example.api_monitoring_starter.Application.DTO.ApplicationInfoDTO;
import com.example.api_monitoring_starter.Application.Service.ApplicationMonitoringService;
import com.example.api_monitoring_starter.Application.Logging.MemoryLogAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;

import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    // LIVE LOGS
    // =========================================================

    @GetMapping("/logs")
    public List<ILoggingEvent> logs() {

        return MemoryLogAppender.getLogs();
    }
}