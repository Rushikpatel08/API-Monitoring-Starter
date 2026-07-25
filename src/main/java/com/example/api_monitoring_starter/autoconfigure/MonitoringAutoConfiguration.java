package com.example.api_monitoring_starter.autoconfigure;

import com.example.api_monitoring_starter.Database.Controller.DatabaseController;
import com.example.api_monitoring_starter.Database.Scanner.EntityScannerService;
import com.example.api_monitoring_starter.Database.Service.DatabaseMetadataService;
import com.example.api_monitoring_starter.Service.ApiRegistryService;
import com.example.api_monitoring_starter.Service.OpenApiExportService;
import com.example.api_monitoring_starter.controller.ApiExportController;
import com.example.api_monitoring_starter.controller.MonitoringController;
import com.example.api_monitoring_starter.controller.MonitoringViewController;
import com.example.api_monitoring_starter.exporter.BrunoExportService;
import com.example.api_monitoring_starter.exporter.InsomniaExportService;
import com.example.api_monitoring_starter.exporter.PostmanExportService;
import com.example.api_monitoring_starter.scanner.ApiScanner;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManagerFactory;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;

import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.sql.DataSource;

@AutoConfiguration
@AutoConfigureAfter(HibernateJpaAutoConfiguration.class)
public class MonitoringAutoConfiguration {

    // =========================================================
    // API MONITORING
    // =========================================================

    @Bean
    public ApiScanner apiScanner(
            @Qualifier("requestMappingHandlerMapping")
            RequestMappingHandlerMapping requestMappingHandlerMapping
    ) {
        return new ApiScanner(
                requestMappingHandlerMapping
        );
    }

    @Bean
    public MonitoringController monitoringController(
            ApiScanner scanner
    ) {
        return new MonitoringController(scanner);
    }

    @Bean
    public MonitoringViewController monitoringViewController() {
        return new MonitoringViewController();
    }

    @Bean
    public ApiRegistryService apiRegistryService(
            ApiScanner apiScanner
    ) {
        return new ApiRegistryService(apiScanner);
    }

    @Bean
    public OpenApiExportService openApiExportService(
            ApiRegistryService apiRegistryService
    ) {
        return new OpenApiExportService(
                apiRegistryService
        );
    }

    @Bean
    public BrunoExportService brunoExportService(
            ObjectMapper objectMapper,
            ApiRegistryService apiRegistryService
    ) {
        return new BrunoExportService(
                objectMapper,
                apiRegistryService
        );
    }

    @Bean
    public InsomniaExportService insomniaExportService(
            ApiRegistryService apiRegistryService
    ) {
        return new InsomniaExportService(
                new ObjectMapper(),
                apiRegistryService
        );
    }

    @Bean
    public PostmanExportService postmanExportService(
            ApiRegistryService apiRegistryService
    ) {
        return new PostmanExportService(
                new ObjectMapper(),
                apiRegistryService
        );
    }

    @Bean
    public ApiExportController apiExportController(
            ApiRegistryService apiRegistryService,
            BrunoExportService brunoExportService,
            InsomniaExportService insomniaExportService,
            PostmanExportService postmanExportService,
            OpenApiExportService openApiExportService
    ) {
        return new ApiExportController(
                apiRegistryService,
                brunoExportService,
                insomniaExportService,
                postmanExportService,
                openApiExportService
        );
    }

    // =========================================================
    // DATABASE EXPLORER
    // =========================================================

    @Bean
    @ConditionalOnClass(EntityManagerFactory.class)
    @ConditionalOnBean(EntityManagerFactory.class)
    public EntityScannerService entityScannerService(
            EntityManagerFactory entityManagerFactory
    ) {
        return new EntityScannerService(
                entityManagerFactory
        );
    }

    @Bean
    @ConditionalOnBean(EntityScannerService.class)
    public DatabaseMetadataService databaseMetadataService(
            DataSource dataSource,
            EntityScannerService entityScannerService
    ) {
        return new DatabaseMetadataService(
                dataSource,
                entityScannerService
        );
    }

    @Bean
    @ConditionalOnBean(DatabaseMetadataService.class)
    public DatabaseController databaseController(
            DatabaseMetadataService databaseMetadataService
    ) {
        return new DatabaseController(
                databaseMetadataService
        );
    }
}