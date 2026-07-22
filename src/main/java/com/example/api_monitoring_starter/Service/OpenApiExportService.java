package com.example.api_monitoring_starter.Service;

import com.example.api_monitoring_starter.dto.ApiEndpointDTO;
import com.example.api_monitoring_starter.dto.ControllerDTO;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class OpenApiExportService {


    private final ApiRegistryService apiRegistryService;


    public OpenApiExportService(
            ApiRegistryService apiRegistryService
    ){
        this.apiRegistryService = apiRegistryService;
    }



    public Map<String, Object> generate() {


        Map<String,Object> openApi = new LinkedHashMap<>();


        // OpenAPI Version
        openApi.put(
                "openapi",
                "3.0.1"
        );


        // API Information
        Map<String,Object> info = new LinkedHashMap<>();

        info.put(
                "title",
                "API Monitoring Starter"
        );

        info.put(
                "version",
                "1.0.0"
        );


        openApi.put(
                "info",
                info
        );



        Map<String,Object> paths = new LinkedHashMap<>();


        List<ControllerDTO> controllers =
                apiRegistryService.getApis();



        for(ControllerDTO controller : controllers){


            for(ApiEndpointDTO api : controller.getApis()){



                /*
                 * Export only user/application APIs
                 * Ignore:
                 * /v3/api-docs
                 * /swagger
                 * /actuator
                 * /monitoring
                 * etc.
                 */

                if(api.getApiType() != null
                        &&
                        api.getApiType().equalsIgnoreCase("SYSTEM")){

                    continue;

                }



                Map<String,Object> method =
                        new LinkedHashMap<>();



                // Summary
                method.put(
                        "summary",
                        api.getSummary() != null
                                &&
                                !api.getSummary().isEmpty()
                                ?
                                api.getSummary()
                                :
                                api.getJavaMethod()
                );


                // Description
                if(api.getDescription()!=null
                        &&
                        !api.getDescription().isEmpty()){

                    method.put(
                            "description",
                            api.getDescription()
                    );

                }



                // Response
                method.put(
                        "responses",
                        Map.of(

                                "200",

                                Map.of(
                                        "description",
                                        "Success"
                                )

                        )
                );



                /*
                 * Create OpenAPI path
                 *
                 * Example:
                 *
                 * /student/getStudent:
                 *      get:
                 *          summary: GetStudent
                 *
                 */

                paths.put(

                        api.getEndpoint(),

                        Map.of(

                                api.getHttpMethod()
                                        .toLowerCase(),

                                method

                        )

                );


            }

        }



        openApi.put(
                "paths",
                paths
        );


        return openApi;

    }

}