package com.example.api_monitoring_starter.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiEndpointDTO {


    private String httpMethod;

    private String endpoint;

    private String javaMethod;

    private List<ApiParameterDTO> parameters;

    private ApiResponseDTO response;

}