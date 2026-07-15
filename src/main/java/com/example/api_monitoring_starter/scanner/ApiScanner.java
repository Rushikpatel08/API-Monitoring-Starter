package com.example.api_monitoring_starter.scanner;


import com.example.api_monitoring_starter.dto.ApiInfoDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;
@Component
public class ApiScanner {


    private final RequestMappingHandlerMapping handlerMapping;


    public ApiScanner(RequestMappingHandlerMapping handlerMapping){
        this.handlerMapping = handlerMapping;
    }


    public List<ApiInfoDTO> scan(){

        return handlerMapping
                .getHandlerMethods()
                .entrySet()
                .stream()
                .map(entry -> {

                    var handler = entry.getValue();


                    return new ApiInfoDTO(

                            handler.getBeanType().getSimpleName(),

                            handler.getMethod().getName(),

                            entry.getKey().toString(),

                            handler.getMethod().getName()

                    );

                })
                .toList();

    }

}