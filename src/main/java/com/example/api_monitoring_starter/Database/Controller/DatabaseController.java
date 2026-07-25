package com.example.api_monitoring_starter.Database.Controller;

import com.example.api_monitoring_starter.Database.DTO.ColumnDTO;
import com.example.api_monitoring_starter.Database.DTO.DatabaseDTO;
import com.example.api_monitoring_starter.Database.Service.DatabaseMetadataService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/monitoring/databases")
public class DatabaseController {

    private final DatabaseMetadataService service;

    public DatabaseController(DatabaseMetadataService service) {
        this.service = service;
    }

    @GetMapping
    public DatabaseDTO getDatabase() throws Exception {
        return service.scanDatabase();
    }

    @GetMapping("/table")
    public List<ColumnDTO> getTableDetails(
            @RequestParam(required = false) String catalog,
            @RequestParam(required = false) String schema,
            @RequestParam String table
    ) throws Exception {

        return service.getTableDetails(
                catalog,
                schema,
                table
        );
    }
}