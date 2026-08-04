package com.monitoring.api_monitoring_starter.Database.DTO;

import lombok.Data;
import java.util.List;

@Data
public class SchemaDTO {

    private String schemaName;

    private List<TableDTO> tables;
}