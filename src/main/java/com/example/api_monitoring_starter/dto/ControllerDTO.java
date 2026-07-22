package com.example.api_monitoring_starter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data

public class ControllerDTO {


    private String controller;


    private List<ApiEndpointDTO> apis;


    private String tagName;


    private String tagDescription;



    public ControllerDTO(
            String controller,
            String tagName,
            String tagDescription,
            List<ApiEndpointDTO> apis
    ){

        this.controller=controller;
        this.tagName=tagName;
        this.tagDescription=tagDescription;
        this.apis=apis;

    }
    public String getController() {
        return controller;
    }

    public String getTagName() {
        return tagName;
    }

    public String getTagDescription() {
        return tagDescription;
    }

    public List<ApiEndpointDTO> getApis() {
        return apis;
    }
}