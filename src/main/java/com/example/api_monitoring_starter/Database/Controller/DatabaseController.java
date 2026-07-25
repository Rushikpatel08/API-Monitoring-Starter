package com.example.api_monitoring_starter.Database.Controller;

import com.example.api_monitoring_starter.Database.DTO.DatabaseDTO;
import com.example.api_monitoring_starter.Database.Service.DatabaseMetadataService;

import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/monitoring/databases")
public class DatabaseController {


    private final DatabaseMetadataService service;


    public DatabaseController(
            DatabaseMetadataService service
    ){
        this.service = service;
    }



    @GetMapping
    public DatabaseDTO getDatabase()
            throws Exception {


        return service.scanDatabase();

    }


}