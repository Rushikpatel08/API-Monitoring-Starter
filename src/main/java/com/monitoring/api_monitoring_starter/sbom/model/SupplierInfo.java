package com.monitoring.api_monitoring_starter.sbom.model;

public class SupplierInfo {

    private String name;

    public SupplierInfo() {
    }

    public SupplierInfo(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}