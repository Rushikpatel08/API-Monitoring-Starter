package com.example.api_monitoring_starter.autoconfigure;

import com.example.api_monitoring_starter.Service.ApiRegistryService;
import com.example.api_monitoring_starter.Service.OpenApiExportService;
import com.example.api_monitoring_starter.controller.BrunoExportController;
import com.example.api_monitoring_starter.controller.MonitoringController;
import com.example.api_monitoring_starter.controller.MonitoringViewController;
import com.example.api_monitoring_starter.exporter.BrunoExportService;
import com.example.api_monitoring_starter.exporter.InsomniaExportService;
import com.example.api_monitoring_starter.exporter.PostmanExportService;
import com.example.api_monitoring_starter.scanner.ApiScanner;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;


@AutoConfiguration
public class MonitoringAutoConfiguration {


    @Bean
    public ApiScanner apiScanner(
            @Qualifier("requestMappingHandlerMapping")
            RequestMappingHandlerMapping requestMappingHandlerMapping
    ){
        return new ApiScanner(requestMappingHandlerMapping);
    }



    @Bean
    public MonitoringController monitoringController(
            ApiScanner scanner
    ){
        return new MonitoringController(scanner);
    }



    @Bean
    public MonitoringViewController monitoringViewController() {

        return new MonitoringViewController();

    }
    @Bean
    public OpenApiExportService openApiExportService(
            ApiRegistryService apiRegistryService
    ) {
        return new OpenApiExportService(apiRegistryService);
    }


    @Bean
    public ApiRegistryService apiRegistryService(
            ApiScanner apiScanner
    ){

        return new ApiRegistryService(apiScanner);

    }



    @Bean
    public BrunoExportService brunoExportService(
            ObjectMapper objectMapper,
            ApiRegistryService apiRegistryService
    ){

        return new BrunoExportService(
                objectMapper,
                apiRegistryService
        );

    }



    @Bean
    public BrunoExportController brunoExportController(
            ApiRegistryService apiRegistryService,
            BrunoExportService brunoExportService,
            InsomniaExportService insomniaExportService,
            PostmanExportService postmanExportService,
            OpenApiExportService openApiExportService
    ) {

        return new BrunoExportController(
                apiRegistryService,
                brunoExportService,
                insomniaExportService,
                postmanExportService,
                openApiExportService
        );
    }

    @Bean
    public InsomniaExportService insomniaExportService(ApiRegistryService apiRegistryService) {
        return new InsomniaExportService(new ObjectMapper(), apiRegistryService);
    }

    @Bean
    public PostmanExportService postmanExportService(ApiRegistryService apiRegistryService) {
        return new PostmanExportService(new ObjectMapper(), apiRegistryService);
    }

}