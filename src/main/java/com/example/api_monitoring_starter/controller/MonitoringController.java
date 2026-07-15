package com.example.api_monitoring_starter.controller;

import com.example.api_monitoring_starter.dto.ApiControllerDTO;
import com.example.api_monitoring_starter.scanner.ApiScanner;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/monitoring")
public class MonitoringController {


    private final ApiScanner apiScanner;


    public MonitoringController(ApiScanner apiScanner){
        this.apiScanner = apiScanner;
    }


    @GetMapping("/apis")
    public List<ApiControllerDTO> getApis(){

        return apiScanner.scan();

    }

}