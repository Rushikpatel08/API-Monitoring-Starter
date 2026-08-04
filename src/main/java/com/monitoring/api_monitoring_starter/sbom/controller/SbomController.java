package com.monitoring.api_monitoring_starter.sbom.controller;


import com.monitoring.api_monitoring_starter.sbom.model.Bom;
import com.monitoring.api_monitoring_starter.sbom.service.SbomService;

import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/monitoring")
public class SbomController {


    private final SbomService service;


    public SbomController(
            SbomService service
    ){

        this.service = service;

    }


    @GetMapping("/sbom")
    public Bom getSbom(){

        return service.generate();

    }

}