package com.example.api_monitoring_starter.scanner;


import com.example.api_monitoring_starter.dto.ApiInfoDTO;
import com.example.api_monitoring_starter.dto.ControllerDTO;
import com.example.api_monitoring_starter.dto.ApiEndpointDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ApiScanner {

    private static final String STARTER_PACKAGE =
            "com.example.api_monitoring_starter";
    private final RequestMappingHandlerMapping handlerMapping;


    public ApiScanner(
            @Qualifier("requestMappingHandlerMapping")
            RequestMappingHandlerMapping handlerMapping
    ){

        this.handlerMapping = handlerMapping;

    }



    public List<ControllerDTO> scan() {


        Map<String, List<ApiEndpointDTO>> grouped =
                new LinkedHashMap<>();


        handlerMapping.getHandlerMethods()
                .forEach((mapping, handler) -> {



                    String packageName =
                            handler.getBeanType()
                                    .getPackageName();



                    // Ignore api-monitoring-starter controllers
                    if(packageName.startsWith(STARTER_PACKAGE)){
                        return;
                    }



                    String controller =
                            handler.getBeanType()
                                    .getSimpleName();



                    String endpoint =
                            mapping.getPatternValues()
                                    .stream()
                                    .findFirst()
                                    .orElse("");



                    String method =
                            mapping.getMethodsCondition()
                                    .getMethods()
                                    .stream()
                                    .findFirst()
                                    .map(Enum::name)
                                    .orElse("REQUEST");



                    grouped.computeIfAbsent(
                                    controller,
                                    k -> new ArrayList<>()
                            )
                            .add(
                                    new ApiEndpointDTO(
                                            method,
                                            endpoint,
                                            handler.getMethod().getName()
                                    )
                            );


                });



        return grouped.entrySet()
                .stream()
                .map(e ->
                        new ControllerDTO(
                                e.getKey(),
                                e.getValue()
                        )
                )
                .toList();

    }

}