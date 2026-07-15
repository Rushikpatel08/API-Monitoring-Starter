package com.example.api_monitoring_starter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiControllerDTO {
    private String controller;
    private String description;
    private List<ApiEndpointDTO> endpoints;
}
