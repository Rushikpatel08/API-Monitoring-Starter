package com.example.api_monitoring_starter.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequestMapping("/monitoring")
public class MonitoringViewController {
    @GetMapping("/ui")
    public String dashboard(){
        return "forward:/monitoring-ui/index.html";
    }
}
