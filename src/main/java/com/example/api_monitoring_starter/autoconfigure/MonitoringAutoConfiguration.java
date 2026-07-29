package com.example.api_monitoring_starter.autoconfigure;

import com.example.api_monitoring_starter.Application.Controller.ApplicationMonitoringController;
import com.example.api_monitoring_starter.Application.Service.ApplicationMonitoringService;
import com.example.api_monitoring_starter.Database.Controller.DatabaseController;
import com.example.api_monitoring_starter.Database.Provider.*;
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

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.EntityManagerFactory;

import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import org.springframework.core.env.Environment;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import javax.sql.DataSource;
import org.springframework.beans.factory.ObjectProvider;
import java.util.List;



@AutoConfiguration
@AutoConfigureAfter(HibernateJpaAutoConfiguration.class)
@ConditionalOnProperty(name = "api.monitoring.enabled", havingValue = "true", matchIfMissing = true)
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
    @ConditionalOnProperty(name = "api.monitoring.database.enabled", havingValue = "true", matchIfMissing = true)
    public EntityScannerService entityScannerService(
            EntityManagerFactory entityManagerFactory
    ) {

        return new EntityScannerService(
                entityManagerFactory
        );
    }




    // =========================================================
    // DATABASE STORAGE PROVIDERS
    // =========================================================


    @Bean
    @ConditionalOnProperty(name = "api.monitoring.database.enabled", havingValue = "true", matchIfMissing = true)
    public MySQLStorageProvider mySQLStorageProvider(){

        return new MySQLStorageProvider();

    }



    @Bean
    @ConditionalOnProperty(name = "api.monitoring.database.enabled", havingValue = "true", matchIfMissing = true)
    public PostgreSQLStorageProvider postgreSQLStorageProvider(){

        return new PostgreSQLStorageProvider();

    }



    @Bean
    @ConditionalOnProperty(name = "api.monitoring.database.enabled", havingValue = "true", matchIfMissing = true)
    public OracleStorageProvider oracleStorageProvider(){

        return new OracleStorageProvider();

    }



    @Bean
    @ConditionalOnProperty(name = "api.monitoring.database.enabled", havingValue = "true", matchIfMissing = true)
    public SQLServerStorageProvider sqlServerStorageProvider(){

        return new SQLServerStorageProvider();

    }



    @Bean
    @ConditionalOnProperty(name = "api.monitoring.database.enabled", havingValue = "true", matchIfMissing = true)
    public H2StorageProvider h2StorageProvider(){

        return new H2StorageProvider();

    }




    @Bean
    @ConditionalOnBean(EntityScannerService.class)
    @ConditionalOnProperty(name = "api.monitoring.database.enabled", havingValue = "true", matchIfMissing = true)
    public DatabaseMetadataService databaseMetadataService(
            DataSource dataSource,
            EntityScannerService entityScannerService,
            List<DatabaseStorageProvider> storageProviders
    ) {


        System.out.println(
                "Loaded Storage Providers: "
                        + storageProviders
        );


        return new DatabaseMetadataService(
                dataSource,
                entityScannerService,
                storageProviders
        );
    }


    @Bean
    @ConditionalOnProperty(
            name = "api.monitoring.application.enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public ApplicationMonitoringService applicationMonitoringService(

            Environment environment,
            ApplicationContext applicationContext,
            ObjectProvider<HealthEndpoint> healthEndpoint,
            DataSource dataSource,
            MeterRegistry meterRegistry

    ) {

        return new ApplicationMonitoringService(

                environment,
                applicationContext,
                healthEndpoint,
                dataSource,
                meterRegistry

        );
    }

    @Bean
    @ConditionalOnProperty(name = "api.monitoring.application.enabled", havingValue = "true", matchIfMissing = true)
    public ApplicationMonitoringController applicationMonitoringController(

            ApplicationMonitoringService service

    ){

        return new ApplicationMonitoringController(service);

    }

    @Bean
    @ConditionalOnBean(DatabaseMetadataService.class)
    @ConditionalOnProperty(name = "api.monitoring.database.enabled", havingValue = "true", matchIfMissing = true)
    public DatabaseController databaseController(
            DatabaseMetadataService databaseMetadataService
    ) {

        return new DatabaseController(
                databaseMetadataService
        );
    }

}