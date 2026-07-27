package com.example.api_monitoring_starter.Database.DTO;

import lombok.Data;

import java.util.List;
//
@Data
public class ColumnDTO {

    private String columnName;

    private String dataType;

    private String sqlType;

    private Integer size;

    private Integer decimalDigits;

    private boolean nullable;

    private String defaultValue;

    private boolean autoIncrement;

    private boolean primaryKey;

    private boolean foreignKey;
    private List<String> validations;
}