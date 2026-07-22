package com.example.api_monitoring_starter.controller;


import com.example.api_monitoring_starter.dto.ApiEndpointDTO;
import com.example.api_monitoring_starter.Service.ApiRegistryService;
import com.example.api_monitoring_starter.exporter.BrunoExportService;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.example.api_monitoring_starter.Service.OpenApiExportService;
import java.util.Map;


@RestController
@RequestMapping("/monitoring")
public class BrunoExportController {


    private final ApiRegistryService registryService;

    private final BrunoExportService brunoExportService;

    private final OpenApiExportService openApiExportService;


    public BrunoExportController(
            ApiRegistryService registryService,
            BrunoExportService brunoExportService,
            OpenApiExportService openApiExportService
    ){

        this.registryService = registryService;
        this.brunoExportService = brunoExportService;
        this.openApiExportService=openApiExportService;

    }




    @GetMapping("/export/bruno/{id}")
    public ResponseEntity<byte[]> exportBruno(
            @PathVariable String id
    ){

        System.out.println("BRUNO EXPORT CALLED: " + id);


        ApiEndpointDTO api =
                registryService.findApi(id);



        if(api == null){

            return ResponseEntity.notFound().build();

        }



        String content =
                brunoExportService.generate(api);



        byte[] file =
                content.getBytes();



        return ResponseEntity.ok()

                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename="
                                +api.getJavaMethod()
                                +".bru"
                )

                .contentType(
                        MediaType.TEXT_PLAIN
                )

                .body(file);

    }

    @GetMapping("/export/bruno/collection")
    public ResponseEntity<byte[]> exportBrunoCollection() {


        byte[] zip =
                brunoExportService.generateCollection();


        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=Student-API-Bruno.zip"
                )
                .contentType(
                        MediaType.APPLICATION_OCTET_STREAM
                )
                .body(zip);

    }
    @GetMapping(value = "/export/openapi", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> exportOpenApi() {
        return openApiExportService.generate();
    }

}