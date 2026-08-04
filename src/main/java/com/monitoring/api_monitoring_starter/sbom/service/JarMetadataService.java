package com.monitoring.api_monitoring_starter.sbom.service;

import com.monitoring.api_monitoring_starter.sbom.model.ExternalReference;
import com.monitoring.api_monitoring_starter.sbom.model.LicenseInfo;
import com.monitoring.api_monitoring_starter.sbom.model.SupplierInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

@Service
public class JarMetadataService {

    @Autowired
    private LicenseScannerService licenseScannerService;


    @Autowired
    private SupplierScannerService supplierScannerService;


    @Autowired
    private ExternalReferenceService externalReferenceService;
    public SupplierInfo readSupplier(ClassLoader classLoader, String jarPath) {

        try (InputStream input = classLoader.getResourceAsStream(jarPath)) {

            if (input == null) {
                return null;
            }

            Manifest manifest = new Manifest(input);

            Attributes attributes = manifest.getMainAttributes();

            String vendor = attributes.getValue("Implementation-Vendor");

            if (vendor == null) {
                vendor = attributes.getValue("Specification-Vendor");
            }

            if (vendor == null) {
                return null;
            }

            return new SupplierInfo(vendor);

        } catch (Exception ex) {
            return null;
        }
    }

    public List<LicenseInfo> readLicenses() {

        List<LicenseInfo> list = new ArrayList<>();

        /*
         * Part 2 will populate this dynamically
         * from pom.xml or MANIFEST.
         */

        return list;
    }

    public List<ExternalReference> readExternalReferences() {

        List<ExternalReference> list = new ArrayList<>();

        /*
         * Part 2 will populate these dynamically
         * from pom.xml.
         */

        return list;
    }

    public List<LicenseInfo> scanLicenses(JarFile jar){

        return licenseScannerService
                .scanLicenses(jar);

    }


    public SupplierInfo scanSupplier(JarFile jar){

        return supplierScannerService
                .scanSupplier(jar);

    }


    public List<ExternalReference> scanReferences(JarFile jar){

        return externalReferenceService
                .scanReferences(jar);

    }
}