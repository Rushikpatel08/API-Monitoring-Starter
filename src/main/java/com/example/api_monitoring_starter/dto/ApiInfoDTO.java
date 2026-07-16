package com.example.api_monitoring_starter.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiInfoDTO {


    private String controller;
    private String httpMethod;
    private String path;

    private List<String> parameters;

    private Object response;

}