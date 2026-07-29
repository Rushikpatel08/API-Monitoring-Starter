package com.example.api_monitoring_starter.Application.DTO;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ApplicationInfoDTO {

    // Application
    private String applicationName;
    private String springBootVersion;
    private String javaVersion;
    private String javaVendor;
    private List<String> activeProfiles;

    // Health
    private String status;
    private Map<String, Object> healthDetails;

    // JVM
    private long totalMemory;
    private long freeMemory;
    private long usedMemory;
    private long maxMemory;

    // Runtime
    private int availableProcessors;
    private int threadCount;
    private long uptime;

    // System
    private String operatingSystem;
    private String osVersion;
    private String architecture;

    // Process
    private String processId;

    // Spring
    private int beanCount;

    // Database
    private String databaseProduct;
    private String databaseStatus;

    // Environment
    private String serverPort;
    private String contextPath;
    private Map<String, String> environment;

    // Metrics
    private double httpRequests;
    private double httpErrors;
    private double jvmMemoryUsed;
    private double jvmMemoryMax;
    private double jvmThreads;
}