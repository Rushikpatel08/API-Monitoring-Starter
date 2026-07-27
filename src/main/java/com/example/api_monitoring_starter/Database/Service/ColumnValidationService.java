package com.example.api_monitoring_starter.Database.Service;

import com.example.api_monitoring_starter.Database.DTO.ColumnValidationDTO;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ColumnValidationService {

    private final Map<String, Map<String, ColumnValidationDTO>> columnMappings
            = new HashMap<>();


    public ColumnValidationDTO getColumnInfo(
            String tableName,
            String columnName
    ) {

        Map<String, ColumnValidationDTO> columns =
                columnMappings.get(
                        normalize(tableName)
                );

        if (columns == null) {
            return null;
        }

        return columns.get(
                normalize(columnName)
        );
    }


    private String normalize(String value) {
        return value == null ? null : value.toLowerCase().trim();
    }
}
