package com.monitoring.api_monitoring_starter.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.monitoring.api_monitoring_starter.security.SbomGeneratorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/monitoring/security")
public class SbomController {


    private final SbomGeneratorService service;


    public SbomController(
            SbomGeneratorService service
    ){
        this.service = service;
    }



    @GetMapping("/sbom")
    public ObjectNode sbom(){

        return service.generate();

    }

}