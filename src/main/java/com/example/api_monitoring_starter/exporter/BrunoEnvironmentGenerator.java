package com.example.api_monitoring_starter.exporter;


import org.springframework.stereotype.Service;



@Service
public class BrunoEnvironmentGenerator {



    public String generate(){


        return """

vars {


baseUrl: http://localhost:8080


token:


apiKey:


username:


password:


}


""";


    }


}