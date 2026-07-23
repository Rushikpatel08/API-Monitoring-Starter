package com.example.api_monitoring_starter.controller;


import com.example.api_monitoring_starter.dto.ApiEndpointDTO;
import com.example.api_monitoring_starter.Service.ApiRegistryService;
import com.example.api_monitoring_starter.exporter.BrunoExportService;
import com.example.api_monitoring_starter.exporter.InsomniaExportService;
import com.example.api_monitoring_starter.exporter.PostmanExportService;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.example.api_monitoring_starter.Service.OpenApiExportService;

import java.nio.charset.StandardCharsets;
import java.util.Map;


@RestController
@RequestMapping("/monitoring")
public class ApiExportController {


    private final ApiRegistryService registryService;

    private final BrunoExportService brunoExportService;

    private final InsomniaExportService insomniaExportService;

    private final PostmanExportService postmanExportService;

    private final OpenApiExportService openApiExportService;


    public ApiExportController(
            ApiRegistryService registryService,
            BrunoExportService brunoExportService,
            InsomniaExportService insomniaExportService,
            PostmanExportService postmanExportService,
            OpenApiExportService openApiExportService
    ){

        this.registryService = registryService;
        this.brunoExportService = brunoExportService;
        this.insomniaExportService = insomniaExportService;
        this.postmanExportService = postmanExportService;
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

    @GetMapping("/export/bruno/collection/{type}")
    public ResponseEntity<byte[]> exportBrunoCollection(
            @PathVariable String type
    ){


        byte[] zip =
                brunoExportService.generateCollection(type);


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
    @GetMapping("/export/insomnia/{id}")
    public ResponseEntity<byte[]> exportInsomnia(@PathVariable String id) {
        ApiEndpointDTO api = registryService.findApi(id);
        if (api == null) {
            return ResponseEntity.notFound().build();
        }

        String content = insomniaExportService.generate(api);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + api.getJavaMethod() + ".json")
                .contentType(MediaType.APPLICATION_JSON)
                .body(content.getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/export/insomnia/collection/{type}")
    public ResponseEntity<byte[]> exportInsomniaCollection(
            @PathVariable String type
    ) {

        String content =
                insomniaExportService.generateCollection(type);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=Student-API-Insomnia.json"
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(content.getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/export/postman/{id}")
    public ResponseEntity<byte[]> exportPostman(@PathVariable String id) {
        ApiEndpointDTO api = registryService.findApi(id);
        if (api == null) {
            return ResponseEntity.notFound().build();
        }

        String content = postmanExportService.generate(api);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + api.getJavaMethod() + ".json")
                .contentType(MediaType.APPLICATION_JSON)
                .body(content.getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/export/postman/collection/{type}")
    public ResponseEntity<byte[]> exportPostmanCollection(
            @PathVariable String type
    ) {
        String content = postmanExportService.generateCollection(type);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Student-API-Postman.json")
                .contentType(MediaType.APPLICATION_JSON)
                .body(content.getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping(value = "/export/openapi", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> exportOpenApi() {
        return openApiExportService.generate();
    }

}