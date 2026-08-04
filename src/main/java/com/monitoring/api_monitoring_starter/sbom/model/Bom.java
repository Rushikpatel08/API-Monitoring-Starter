package com.monitoring.api_monitoring_starter.sbom.model;

import lombok.Data;

import java.util.List;
import java.util.Map;


@Data
public class Bom {


    private String bomFormat="CycloneDX";

    private String specVersion="1.5";

    private String serialNumber;

    private int version=1;

    private String timestamp;


    private Map<String,Object> metadata;


    private List<Component> components;


}