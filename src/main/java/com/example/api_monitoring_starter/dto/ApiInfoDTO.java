package com.example.api_monitoring_starter.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiInfoDTO {


    private String controller;

    private String httpMethod;

    private String endpoint;

    private String javaMethod;

}