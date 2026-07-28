package com.example.api_monitoring_starter.Database.DTO;

import lombok.Data;
import java.util.List;

@Data
public class DatabaseDTO {

    // Existing
    private String databaseName;
    private String databaseVersion;
    private String driverName;
    private String url;

    // New
    private String username;

    private String catalog;

    private String currentSchema;

    private String databaseProduct;

    private String driverVersion;

    private String jdbcVersion;

    private String transactionIsolation;

    private boolean autoCommit;

    private boolean readOnly;

    private Long totalSizeBytes;

    private Long freeSizeBytes;

    private Long usableSizeBytes;

    private int schemaCount;

    private int tableCount;

    private int viewCount;

    private List<SchemaDTO> schemas;
}