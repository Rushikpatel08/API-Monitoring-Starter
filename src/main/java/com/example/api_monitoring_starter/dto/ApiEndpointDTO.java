package com.example.api_monitoring_starter.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
public class ApiEndpointDTO {


    private String id;


    private String httpMethod;


    private String endpoint;


    private String javaMethod;


    private List<ApiParameterDTO> parameters;


    private ApiResponseDTO response;


    private ApiRequestDTO request;


    private ApiAuthDTO authentication;


    private String summary;


    private String description;



    public ApiEndpointDTO(
            String id,
            String httpMethod,
            String endpoint,
            String javaMethod,
            List<ApiParameterDTO> parameters,
            ApiResponseDTO response,
            ApiRequestDTO request,
            ApiAuthDTO authentication,
            String summary,
            String description
    ){

        this.id=id;
        this.httpMethod=httpMethod;
        this.endpoint=endpoint;
        this.javaMethod=javaMethod;
        this.parameters=parameters;
        this.response=response;
        this.request=request;
        this.authentication=authentication;
        this.summary=summary;
        this.description=description;

    }
    public String getId(){
        return id;
    }

    public String getSummary(){
        return summary;
    }


    public String getDescription(){
        return description;
    }

}