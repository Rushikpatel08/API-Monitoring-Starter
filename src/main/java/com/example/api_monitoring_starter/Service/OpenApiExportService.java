package com.example.api_monitoring_starter.Service;

import com.example.api_monitoring_starter.dto.ApiEndpointDTO;
import com.example.api_monitoring_starter.dto.ControllerDTO;

import java.util.*;

public class OpenApiExportService {


    private final ApiRegistryService apiRegistryService;


    public OpenApiExportService(
            ApiRegistryService apiRegistryService
    ){
        this.apiRegistryService = apiRegistryService;
    }



    public Map<String, Object> generate() {


        Map<String,Object> openApi = new LinkedHashMap<>();

        openApi.put("openapi","3.0.1");


        Map<String,Object> info = new LinkedHashMap<>();
        info.put("title","API Monitoring Starter");
        info.put("version","1.0.0");

        openApi.put("info",info);



        Map<String,Object> paths = new LinkedHashMap<>();


        List<ControllerDTO> controllers =
                apiRegistryService.getApis();



        for(ControllerDTO controller : controllers){


            for(ApiEndpointDTO api : controller.getApis()){


                Map<String,Object> method = new LinkedHashMap<>();


                method.put(
                        "summary",
                        api.getJavaMethod()
                );


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


                paths.put(
                        api.getEndpoint(),
                        Map.of(
                                api.getHttpMethod().toLowerCase(),
                                method
                        )
                );

            }

        }


        openApi.put("paths", paths);


        return openApi;

    }

}