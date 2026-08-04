package com.monitoring.api_monitoring_starter.sbom.model;

public class ExternalReference {

    private String type;
    private String url;

    public ExternalReference() {
    }

    public ExternalReference(String type, String url) {
        this.type = type;
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}