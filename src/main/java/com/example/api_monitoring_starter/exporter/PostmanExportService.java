package com.example.api_monitoring_starter.exporter;

import com.example.api_monitoring_starter.Service.ApiRegistryService;
import com.example.api_monitoring_starter.dto.ApiEndpointDTO;
import com.example.api_monitoring_starter.dto.ControllerDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PostmanExportService {

    private final ObjectMapper objectMapper;
    private final ApiRegistryService apiRegistryService;


    public PostmanExportService(
            ObjectMapper objectMapper,
            ApiRegistryService apiRegistryService
    ) {
        this.objectMapper = objectMapper;
        this.apiRegistryService = apiRegistryService;
    }



    public String generate(ApiEndpointDTO api) {

        try {

            Map<String,Object> item = new LinkedHashMap<>();

            item.put(
                    "name",
                    api.getJavaMethod()
            );

            item.put(
                    "request",
                    buildRequest(api)
            );


            return objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(
                            Map.of(
                                    "info",
                                    Map.of(
                                            "name",
                                            "Student API",
                                            "schema",
                                            "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
                                    ),
                                    "item",
                                    List.of(item)
                            )
                    );


        }
        catch(Exception e){

            throw new RuntimeException(
                    "Failed creating Postman export",
                    e
            );

        }

    }



    public String generateCollection(){

        return generateCollection("all");

    }




    public String generateCollection(String type){


        try{


            List<Map<String,Object>> items =
                    new ArrayList<>();


            for(ControllerDTO controller :
                    apiRegistryService.getApis()){


                for(ApiEndpointDTO api :
                        controller.getApis()){


                    if(type != null &&
                            !type.equalsIgnoreCase("all")){


                        if(!api.getApiType()
                                .equalsIgnoreCase(type)){

                            continue;

                        }

                    }


                    Map<String,Object> item =
                            new LinkedHashMap<>();


                    item.put(
                            "name",
                            api.getJavaMethod()
                    );


                    item.put(
                            "request",
                            buildRequest(api)
                    );


                    items.add(item);

                }

            }



            Map<String,Object> collection =
                    new LinkedHashMap<>();


            collection.put(
                    "info",
                    Map.of(
                            "name",
                            "Student API",
                            "schema",
                            "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
                    )
            );


            collection.put(
                    "variable",
                    List.of(
                            Map.of(
                                    "key",
                                    "baseUrl",
                                    "value",
                                    getBaseUrl()
                            )
                    )
            );


            collection.put(
                    "item",
                    items
            );


            return objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(collection);


        }
        catch(Exception e){

            throw new RuntimeException(
                    "Failed creating Postman collection",
                    e
            );

        }

    }







    private Map<String, Object> buildRequest(ApiEndpointDTO api) throws Exception {

        Map<String, Object> request = new LinkedHashMap<>();

        request.put(
                "method",
                api.getHttpMethod().toUpperCase()
        );

        request.put(
                "header",
                List.of()
        );


        // Request Body
        if (api.getRequest() != null) {

            Map<String, Object> body = new LinkedHashMap<>();

            body.put(
                    "mode",
                    "raw"
            );

            body.put(
                    "raw",
                    objectMapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(
                                    api.getRequest().getExample()
                            )
            );

            Map<String,Object> options = new LinkedHashMap<>();
            options.put("raw", Map.of(
                    "language",
                    "json"
            ));

            body.put(
                    "options",
                    options
            );

            request.put(
                    "body",
                    body
            );

        }


        // URL
        String endpoint = api.getEndpoint();

        String baseUrl = getBaseUrl();

        String fullUrl = baseUrl + endpoint;


        Map<String,Object> url = new LinkedHashMap<>();

        url.put(
                "raw",
                fullUrl
        );


        url.put(
                "host",
                List.of(
                        baseUrl
                )
        );


        url.put(
                "path",
                List.of(
                        endpoint.replaceFirst("^/", "")
                )
        );


        request.put(
                "url",
                url
        );


        return request;
    }



    private String getBaseUrl() {

        ServletRequestAttributes attributes =
                (ServletRequestAttributes)
                        RequestContextHolder.getRequestAttributes();

        if(attributes != null){

            return attributes
                    .getRequest()
                    .getScheme()
                    + "://"
                    + attributes.getRequest().getServerName()
                    + ":"
                    + attributes.getRequest().getServerPort();

        }

        return "http://localhost:8080";
    }
}