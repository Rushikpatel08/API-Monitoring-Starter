package com.example.api_monitoring_starter.Database.DTO;

import lombok.Data;
import java.util.List;

@Data
public class DatabaseDTO {

    private String databaseName;


    private String databaseVersion;

    private String driverName;

    private String url;

    private List<SchemaDTO> schemas;
}