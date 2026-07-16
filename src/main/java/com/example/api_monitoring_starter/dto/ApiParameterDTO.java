package com.example.api_monitoring_starter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiParameterDTO {

    private String name;
    private String type;
    private String dataType;
    private boolean required;
}