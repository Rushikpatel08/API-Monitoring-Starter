package com.example.api_monitoring_starter.Service;


import com.example.api_monitoring_starter.dto.*;
import com.example.api_monitoring_starter.scanner.ApiScanner;

import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class ApiRegistryService {


    private final ApiScanner apiScanner;



    public ApiRegistryService(ApiScanner apiScanner){

        this.apiScanner = apiScanner;

    }

    public List<ControllerDTO> getApis(){

        return apiScanner.scan();

    }


    public ApiEndpointDTO findApi(String id){


        List<ControllerDTO> controllers =
                apiScanner.scan();



        for(ControllerDTO controller: controllers){


            for(ApiEndpointDTO api :
                    controller.getApis()){


                if(api.getId().equals(id)){


                    return api;

                }

            }

        }


        return null;

    }


}