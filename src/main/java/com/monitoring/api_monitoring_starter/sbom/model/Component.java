package com.monitoring.api_monitoring_starter.sbom.model;
import com.monitoring.api_monitoring_starter.sbom.model.SupplierInfo;
import com.monitoring.api_monitoring_starter.sbom.model.LicenseInfo;
import com.monitoring.api_monitoring_starter.sbom.model.ExternalReference;

import lombok.Data;
import java.util.List;
import java.util.ArrayList;
import java.util.List;
@Data
public class Component {


    private String type;

    private String group;

    private String name;

    private String version;

    private String scope;

    private String purl;

    private List<Hash> hashes;

    private SupplierInfo supplier;


}