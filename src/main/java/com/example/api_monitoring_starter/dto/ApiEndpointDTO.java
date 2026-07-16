package com.example.api_monitoring_starter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiEndpointDTO {

    private String httpMethod;

    private String endpoint;

    private String javaMethod;

}
