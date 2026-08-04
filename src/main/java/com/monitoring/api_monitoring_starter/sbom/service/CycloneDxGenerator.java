package com.monitoring.api_monitoring_starter.sbom.service;


import com.monitoring.api_monitoring_starter.sbom.model.DependencyInfo;
import com.monitoring.api_monitoring_starter.sbom.model.ExternalReference;
import com.monitoring.api_monitoring_starter.sbom.model.LicenseInfo;


import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;



@Service
public class CycloneDxGenerator {


    private final DependencyScannerService dependencyScannerService;



    public CycloneDxGenerator(
            DependencyScannerService dependencyScannerService
    ){

        this.dependencyScannerService =
                dependencyScannerService;

    }



    public Map<String,Object> generate(){


        Map<String,Object> bom =
                new LinkedHashMap<>();


        bom.put(
                "bomFormat",
                "CycloneDX"
        );


        bom.put(
                "specVersion",
                "1.5"
        );


        bom.put(
                "version",
                1
        );



        List<Map<String,Object>> components =
                new ArrayList<>();



        List<DependencyInfo> dependencies =
                dependencyScannerService
                        .scanDependencies();



        for(DependencyInfo dependency : dependencies){



            Map<String,Object> component =
                    new LinkedHashMap<>();



            component.put(
                    "type",
                    "library"
            );



            component.put(
                    "name",
                    dependency.getArtifact()
            );



            component.put(
                    "version",
                    dependency.getVersion()
            );



            if(dependency.getGroup()!=null){


                component.put(
                        "group",
                        dependency.getGroup()
                );

            }



            /*
             * LICENSES
             */


            if(dependency.getLicenses()!=null
                    &&
                    !dependency.getLicenses().isEmpty()){


                List<Map<String,Object>> licenses =
                        new ArrayList<>();



                for(LicenseInfo license :
                        dependency.getLicenses()){



                    Map<String,Object> licenseObject =
                            new LinkedHashMap<>();


                    Map<String,String> licenseDetails =
                            new LinkedHashMap<>();


                    licenseDetails.put(
                            "name",
                            license.getName()
                    );


                    if(license.getId()!=null){

                        licenseDetails.put(
                                "id",
                                license.getId()
                        );

                    }



                    licenseObject.put(
                            "license",
                            licenseDetails
                    );



                    licenses.add(
                            licenseObject
                    );

                }



                component.put(
                        "licenses",
                        licenses
                );

            }




            /*
             * SUPPLIER
             */


            if(dependency.getSupplier()!=null){


                component.put(
                        "supplier",
                        Map.of(
                                "name",
                                dependency
                                        .getSupplier()
                                        .getName()
                        )
                );

            }




            /*
             * EXTERNAL REFERENCES
             */


            if(dependency.getExternalReferences()!=null
                    &&
                    !dependency.getExternalReferences().isEmpty()){



                List<Map<String,String>> references =
                        new ArrayList<>();



                for(ExternalReference reference :
                        dependency.getExternalReferences()){


                    Map<String,String> ref =
                            new LinkedHashMap<>();


                    ref.put(
                            "type",
                            reference.getType()
                    );


                    ref.put(
                            "url",
                            reference.getUrl()
                    );


                    references.add(ref);


                }



                component.put(
                        "externalReferences",
                        references
                );


            }



            components.add(component);


        }



        bom.put(
                "components",
                components
        );



        return bom;

    }


}