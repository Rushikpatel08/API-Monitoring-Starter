package com.example.api_monitoring_starter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiEndpointDTO {
    private String httpMethod;
    private String path;
    private String summary;
    private List<ApiParameterDTO> params;
    private String returnType;
}
