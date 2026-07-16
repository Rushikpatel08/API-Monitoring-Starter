package com.example.api_monitoring_starter.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponseDTO {


    private int code;

    private String description;

    private String mediaType;

    private Object example;

    private Object schema;

    private Object links;

}