package com.example.api_monitoring_starter.autoconfigure;

import com.example.api_monitoring_starter.controller.MonitoringController;
import com.example.api_monitoring_starter.controller.MonitoringViewController;
import com.example.api_monitoring_starter.scanner.ApiScanner;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Objects;

@AutoConfiguration
public class MonitoringAutoConfiguration {


    @Bean
    public ApiScanner apiScanner( @Qualifier("requestMappingHandlerMapping")
            RequestMappingHandlerMapping requestMappingHandlerMapping){

        return new ApiScanner(requestMappingHandlerMapping);
    }


    @Bean
    public MonitoringController monitoringController(ApiScanner scanner){

        return new MonitoringController(scanner);
    }

    @Bean
    public MonitoringViewController monitoringViewController() {
        return new MonitoringViewController();
    }


}