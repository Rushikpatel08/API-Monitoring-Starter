package com.monitoring.api_monitoring_starter.sbom.model;
import java.util.ArrayList;
import java.util.List;

import com.monitoring.api_monitoring_starter.sbom.model.LicenseInfo;
import com.monitoring.api_monitoring_starter.sbom.model.SupplierInfo;
import com.monitoring.api_monitoring_starter.sbom.model.ExternalReference;
public class DependencyInfo {

    private String group;
    private String artifact;
    private String version;
    private String file;
    private String hash;
    private SupplierInfo supplier;

    private List<LicenseInfo> licenses =
            new ArrayList<>();

    private List<ExternalReference> externalReferences =
            new ArrayList<>();
    public DependencyInfo() {
    }

    public DependencyInfo(
            String group,
            String artifact,
            String version,
            String file,
            String hash
    ) {
        this.group = group;
        this.artifact = artifact;
        this.version = version;
        this.file = file;
        this.hash = hash;
    }

    public SupplierInfo getSupplier() {
        return supplier;
    }

    public void setSupplier(SupplierInfo supplier) {
        this.supplier = supplier;
    }


    public List<LicenseInfo> getLicenses() {
        return licenses;
    }

    public void setLicenses(List<LicenseInfo> licenses) {
        this.licenses = licenses;
    }


    public List<ExternalReference> getExternalReferences() {
        return externalReferences;
    }

    public void setExternalReferences(
            List<ExternalReference> externalReferences) {

        this.externalReferences = externalReferences;
    }
    public String getGroup() {
        return group;
    }

    public String getArtifact() {
        return artifact;
    }

    public String getVersion() {
        return version;
    }

    public String getFile() {
        return file;
    }

    public String getHash() {
        return hash;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setArtifact(String artifact) {
        this.artifact = artifact;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}