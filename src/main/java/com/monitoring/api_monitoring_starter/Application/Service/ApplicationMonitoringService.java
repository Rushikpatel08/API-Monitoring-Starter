package com.monitoring.api_monitoring_starter.Application.Service;

import com.monitoring.api_monitoring_starter.Application.DTO.ApplicationInfoDTO;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.actuate.health.CompositeHealth;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.Timer;
import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.*;

@Service
public class ApplicationMonitoringService {

    private final Environment environment;
    private final ApplicationContext applicationContext;
    private final ObjectProvider<HealthEndpoint> healthEndpointProvider;
    private final DataSource dataSource;
    private final MeterRegistry meterRegistry;

    public ApplicationMonitoringService(
            Environment environment,
            ApplicationContext applicationContext,
            ObjectProvider<HealthEndpoint> healthEndpointProvider,
            DataSource dataSource,
            MeterRegistry meterRegistry
    ) {
        this.environment = environment;
        this.applicationContext = applicationContext;
        this.healthEndpointProvider = healthEndpointProvider;
        this.dataSource = dataSource;
        this.meterRegistry = meterRegistry;
    }
    // =========================================================
    // APPLICATION INFORMATION
    // =========================================================
    public ApplicationInfoDTO getApplicationInfo() {

        ApplicationInfoDTO dto = new ApplicationInfoDTO();
        // -----------------------------------------------------
        // Application
        // -----------------------------------------------------
        dto.setApplicationName(environment.getProperty("spring.application.name","Unknown"));
        dto.setSpringBootVersion(SpringBootVersion.getVersion());
        dto.setJavaVersion(System.getProperty("java.version"));
        dto.setJavaVendor(System.getProperty("java.vendor"));
        dto.setActiveProfiles(Arrays.asList(environment.getActiveProfiles()));
        // -----------------------------------------------------
        // Health
        // -----------------------------------------------------
        HealthEndpoint healthEndpoint = healthEndpointProvider.getIfAvailable();
        if (healthEndpoint != null) {
            try {
                HealthComponent component = healthEndpoint.health();
                dto.setStatus(component.getStatus().getCode());
                Map<String, Object> healthDetails = new LinkedHashMap<>();
                if (component instanceof CompositeHealth composite) {
                    composite.getComponents()
                            .forEach((name, healthComponent) -> {
                                Map<String, Object> detail =new LinkedHashMap<>();
                                detail.put("status",healthComponent.getStatus().getCode());
                                healthDetails.put(name,detail);
                            });
                }
                else if (component instanceof Health health) {
                    healthDetails.put("details",health.getDetails());
                }
                dto.setHealthDetails(healthDetails);
            }
            catch (Exception e) {
                dto.setStatus("UNKNOWN");
            }
        } else {
            dto.setStatus("ACTUATOR_NOT_ENABLED");
        }
        // -----------------------------------------------------
        // JVM Memory
        // -----------------------------------------------------
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        dto.setTotalMemory(totalMemory);
        dto.setFreeMemory(freeMemory);
        dto.setUsedMemory(usedMemory);
        dto.setMaxMemory(runtime.maxMemory());
        // -----------------------------------------------------
        // CPU
        // -----------------------------------------------------
        dto.setAvailableProcessors(runtime.availableProcessors());

        // -----------------------------------------------------
        // Threads
        // -----------------------------------------------------
        ThreadMXBean threadBean =ManagementFactory.getThreadMXBean();
        dto.setThreadCount(threadBean.getThreadCount());
        // -----------------------------------------------------
        // Operating System
        // -----------------------------------------------------
        dto.setOperatingSystem(System.getProperty("os.name"));
        dto.setOsVersion(System.getProperty("os.version"));
        dto.setArchitecture(System.getProperty("os.arch"));
        // -----------------------------------------------------
        // Spring
        // -----------------------------------------------------
        dto.setBeanCount(applicationContext.getBeanDefinitionCount());
        // -----------------------------------------------------
        // JVM Process
        // -----------------------------------------------------
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        dto.setProcessId(runtimeBean.getName());
        dto.setUptime(runtimeBean.getUptime());
        // -----------------------------------------------------
        // Database
        // -----------------------------------------------------
        try (Connection connection =dataSource.getConnection())
        {
            DatabaseMetaData metaData = connection.getMetaData();
            dto.setDatabaseProduct(metaData.getDatabaseProductName());
            dto.setDatabaseStatus("CONNECTED");
        }
        catch (Exception e) {
            dto.setDatabaseProduct("Unknown");
            dto.setDatabaseStatus("DOWN");
        }
        // -----------------------------------------------------
        // Environment
        // -----------------------------------------------------
        dto.setServerPort(environment.getProperty("server.port","8080"));
        dto.setContextPath(environment.getProperty("server.servlet.context-path","/"));
        Map<String, String> environmentData = new LinkedHashMap<>();
        addEnvironmentProperty(environmentData,"spring.application.name","Application Name");
        addEnvironmentProperty(environmentData,"spring.profiles.active","Active Profiles");
        addEnvironmentProperty(environmentData,"server.port","Server Port");
        addEnvironmentProperty(environmentData,"server.servlet.context-path","Context Path");
        addEnvironmentProperty(environmentData,"spring.datasource.url","Datasource URL");
        dto.setEnvironment(environmentData);
        // -----------------------------------------------------
        // HTTP Metrics
        // -----------------------------------------------------
        dto.setHttpRequests(getHttpRequestCount());
        dto.setHttpErrors(getHttpErrorCount());
        // -----------------------------------------------------
        // JVM Metrics
        // -----------------------------------------------------
        dto.setJvmMemoryUsed(getJvmMemoryUsed());
        dto.setJvmMemoryMax(getJvmMemoryMax());
        dto.setJvmThreads(getJvmThreads());
        return dto;
    }
    // =========================================================
    // JVM MEMORY USED
    // =========================================================
    private double getJvmMemoryUsed() {
        double total = 0;
        for (Meter meter : meterRegistry.getMeters()) {
            if (!"jvm.memory.used".equals(meter.getId().getName())) {
                continue;
            }
            Double value =meterRegistry
                            .find("jvm.memory.used")
                            .tags(meter.getId().getTags())
                            .gauge()
                            .value();
            if (value != null) {
                total += value;
            }
        }
        return total;
    }
    // =========================================================
    // JVM MEMORY MAX
    // =========================================================
    private double getJvmMemoryMax() {
        double total = 0;
        for (Meter meter : meterRegistry.getMeters()) {
            if (!"jvm.memory.max".equals(meter.getId().getName())) {
                continue;
            }
            Double value =meterRegistry
                            .find("jvm.memory.max")
                            .tags(meter.getId().getTags())
                            .gauge()
                            .value();
            if (value != null && value > 0) {
                total += value;
            }
        }
        return total;
    }
    // =========================================================
    // JVM THREADS
    // =========================================================
    private double getJvmThreads() {
        for (Meter meter : meterRegistry.getMeters()) {
            if (!"jvm.threads.live".equals(meter.getId().getName())) {
                continue;
            }
            Double value = meterRegistry
                            .find("jvm.threads.live")
                            .tags(meter.getId().getTags())
                            .gauge()
                            .value();
            if (value != null) {
                return value;
            }
        }
        return 0;
    }
    // =========================================================
    // HTTP REQUEST COUNT
    // =========================================================
    private double getHttpRequestCount() {
        double total = 0;
        for (Meter meter : meterRegistry.getMeters()) {
            if (!"http.server.requests".equals(meter.getId().getName())) {
                continue;
            }
            Timer timer =meterRegistry
                            .find("http.server.requests")
                            .tags(meter.getId().getTags())
                            .timer();
            if (timer != null) {
                total += timer.count();
            }
        }
        return total;
    }
    // =========================================================
    // ENVIRONMENT
    // =========================================================
    private void addEnvironmentProperty(
            Map<String, String> map,
            String property,
            String displayName
    ) {
        String value =environment.getProperty(property);
        if (value != null) {
            map.put(displayName, value);
        }
    }
    // =========================================================
    // METRICS
    // =========================================================
    private double getCounterValue(String name) {
        try {
            Meter meter =meterRegistry.find(name).meter();
            if (meter == null) {
                return 0;
            }
            return meterRegistry
                    .find(name)
                    .counter()
                    .count();

        } catch (Exception e) {
            return 0;
        }
    }
    private double getGaugeValue(String name) {
        try {
            Double value =meterRegistry
                            .find(name)
                            .gauge()
                            .value();
            return value != null ? value : 0;
        } catch (Exception e) {
            return 0;
        }
    }
    private double getHttpErrorCount() {
        double errors = 0;
        try {
            for (Meter meter : meterRegistry.getMeters()) {
                if (!"http.server.requests".equals(meter.getId().getName())) {
                    continue;
                }
                String status =meter.getId().getTag("status");
                if (status == null) {
                    continue;
                }
                if (status.startsWith("4") || status.startsWith("5")) {
                    Timer timer =meterRegistry
                                    .find("http.server.requests")
                                    .tags(meter.getId().getTags())
                                    .timer();
                    if (timer != null) {
                        errors += timer.count();
                    }
                }
            }
        } catch (Exception e) {
            return 0;
        }
        return errors;
    }
}