package com.example.api_monitoring_starter.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
public class ApiEndpointDTO {


    private String httpMethod;

    private String endpoint;

    private String javaMethod;

    private List<ApiParameterDTO> parameters;

    private ApiResponseDTO response;

    private ApiRequestDTO request;

    private String summary;

    private String description;

    public ApiEndpointDTO(String httpMethod,String endpoint,String javaMethod,List<ApiParameterDTO> parameters,
            ApiResponseDTO response,ApiRequestDTO request,String summary,String description)
    {
        this.httpMethod = httpMethod;
        this.endpoint = endpoint;
        this.javaMethod = javaMethod;
        this.parameters = parameters;
        this.response = response;
        this.request = request;
        this.summary = summary;
        this.description = description;
    }
    public String getSummary(){
        return summary;
    }


    public String getDescription(){
        return description;
    }

}