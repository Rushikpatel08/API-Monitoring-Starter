package com.example.api_monitoring_starter.Application.DTO;


import lombok.Data;


@Data
public class ApplicationLogDTO {


    private String timestamp;

    private String level;

    private String logger;

    private String thread;

    private String message;

    private String exception;


}