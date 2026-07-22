package com.example.api_monitoring_starter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiAuthDTO {


    /*
       Authentication Type

       Examples:
       Bearer
       API_KEY
       BASIC
    */
    private String type;



    /*
       Header name

       Example:
       Authorization
       x-api-key
    */
    private String headerName;



    /*
       Bruno variable name

       Example:
       token
       apiKey
       usernamePassword
    */
    private String variable;

}