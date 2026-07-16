package com.example.api_monitoring_starter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ControllerDTO {

    private String controller;

    private List<ApiEndpointDTO> apis;

}