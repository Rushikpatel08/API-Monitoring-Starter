package com.monitoring.api_monitoring_starter.controller;


import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


//controller for monitoring UI views
@Controller
@RequestMapping("/monitoring")
public class MonitoringViewController {


    @PostConstruct
    public void init() {
        System.out.println("======================================");
        System.out.println("MonitoringViewController CREATED");
        System.out.println("======================================");
    }

    @GetMapping("/ui")
    public String dashboard(){

        return "forward:/monitoring-ui/API-List/index.html";

    }



    @GetMapping("/database")
    public String databaseDashboard(){

        return "forward:/monitoring-ui/Database/index.html";

    }



    @GetMapping("/application")
    public String applicationDashboard(){

        return "forward:/monitoring-ui/Application/index.html";

    }

    @GetMapping("/application-test")
    public String applicationDashboardTest(){

        return "forward:/monitoring-ui/Application/index-test.html";

    }


}