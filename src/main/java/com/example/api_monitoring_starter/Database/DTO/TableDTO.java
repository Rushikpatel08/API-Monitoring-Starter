package com.example.api_monitoring_starter.Database.DTO;

import lombok.Data;

@Data
public class TableDTO {

    private String tableName;

    private String tableType;

    private int columnCount;

    private int primaryKeyCount;

    private int foreignKeyCount;

    private String entityName;
}