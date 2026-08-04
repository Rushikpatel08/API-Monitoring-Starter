package com.monitoring.api_monitoring_starter.sbom.service;


import com.monitoring.api_monitoring_starter.sbom.model.DependencyInfo;

import org.springframework.stereotype.Service;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;



@Service
public class DependencyScannerService {


    private final LicenseScannerService licenseScannerService;

    private final SupplierScannerService supplierScannerService;

    private final ExternalReferenceService externalReferenceService;



    public DependencyScannerService(
            LicenseScannerService licenseScannerService,
            SupplierScannerService supplierScannerService,
            ExternalReferenceService externalReferenceService
    ){

        this.licenseScannerService = licenseScannerService;
        this.supplierScannerService = supplierScannerService;
        this.externalReferenceService = externalReferenceService;

    }



    public List<DependencyInfo> scanDependencies(){


        List<DependencyInfo> dependencies =
                new ArrayList<>();



        String classPath =
                System.getProperty("java.class.path");



        String[] jars =
                classPath.split(
                        File.pathSeparator
                );



        for(String path : jars){



            if(!path.endsWith(".jar")){
                continue;
            }



            File file =
                    new File(path);



            if(!file.exists()){
                continue;
            }



            try(JarFile jar =
                        new JarFile(file)){



                DependencyInfo dependency =
                        new DependencyInfo();



                /*
                 * BASIC INFORMATION
                 */


                dependency.setArtifact(
                        extractName(file.getName())
                );


                dependency.setVersion(
                        extractVersion(file.getName())
                );


                dependency.setFile(
                        file.getAbsolutePath()
                );


                /*
                 * LICENSE INFORMATION
                 */


                dependency.setLicenses(
                        licenseScannerService
                                .scanLicenses(jar)
                );



                /*
                 * SUPPLIER INFORMATION
                 */


                dependency.setSupplier(
                        supplierScannerService
                                .scanSupplier(jar)
                );



                /*
                 * EXTERNAL REFERENCES
                 */


                dependency.setExternalReferences(
                        externalReferenceService
                                .scanReferences(jar)
                );



                /*
                 * ADD DEPENDENCY
                 */


                dependencies.add(dependency);



            }
            catch(Exception e){

                e.printStackTrace();

            }


        }



        return dependencies;

    }





    private String extractName(String jar){


        return jar
                .replace(".jar","")
                .replaceAll("-[0-9].*$","");

    }





    private String extractVersion(String jar){


        String name =
                jar.replace(".jar","");



        String[] parts =
                name.split("-");



        if(parts.length > 1){


            return parts[parts.length - 1];


        }



        return "unknown";

    }


}