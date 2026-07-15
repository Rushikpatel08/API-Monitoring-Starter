package com.example.api_monitoring_starter.autoconfigure;


import com.example.api_monitoring_starter.controller.MonitoringController;
import com.example.api_monitoring_starter.scanner.ApiScanner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;


@AutoConfiguration
public class MonitoringAutoConfiguration {


    @Bean
    public ApiScanner apiScanner(ApplicationContext applicationContext){

        RequestMappingHandlerMapping mapping = applicationContext.getBean(
                "requestMappingHandlerMapping",
                RequestMappingHandlerMapping.class
        );

        return new ApiScanner(mapping);

    }


    @Bean
    public MonitoringController monitoringController(ApiScanner scanner){

        return new MonitoringController(scanner);

    }

}