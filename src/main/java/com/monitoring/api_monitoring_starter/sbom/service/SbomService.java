package com.monitoring.api_monitoring_starter.sbom.service;



import com.monitoring.api_monitoring_starter.sbom.model.*;

import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;


import java.io.File;
import java.time.Instant;
import java.util.*;



@Service
public class SbomService {



    private final DependencyScannerService scanner;

    private final HashGeneratorService hashService;

    private final BuildProperties buildProperties;

    private final Environment environment;




    public SbomService(
            DependencyScannerService scanner,
            HashGeneratorService hashService,
            BuildProperties buildProperties,
            Environment environment
    ){

        this.scanner = scanner;

        this.hashService = hashService;

        this.buildProperties = buildProperties;

        this.environment = environment;

    }





    public Bom generate(){



        Bom bom =
                new Bom();



        bom.setSerialNumber(
                "urn:uuid:"+
                        UUID.randomUUID()
        );



        bom.setTimestamp(
                Instant.now()
                        .toString()
        );





        Map<String,Object> metadata =
                new HashMap<>();


        Map<String,Object> application =
                new HashMap<>();



        application.put(
                "type",
                "application"
        );



        application.put(
                "name",
                getApplicationName()
        );



        application.put(
                "version",
                getVersion()
        );



        application.put(
                "springBootVersion",
                SpringBootVersion.getVersion()
        );



        application.put(
                "javaVersion",
                System.getProperty(
                        "java.version"
                )
        );



        application.put(
                "os",
                System.getProperty(
                        "os.name"
                )
        );



        metadata.put(
                "component",
                application
        );



        bom.setMetadata(
                metadata
        );





        List<Component> components =
                new ArrayList<>();




        for(DependencyInfo dependency :
                scanner.scanDependencies()){



            Component component =
                    new Component();



            component.setType(
                    "library"
            );



            component.setGroup(
                    dependency.getGroup()
            );



            component.setName(
                    dependency.getArtifact()
            );



            component.setVersion(
                    dependency.getVersion()
            );



            component.setScope(
                    "required"
            );



            component.setPurl(

                    "pkg:maven/"
                            +
                            dependency.getGroup()
                            +
                            "/"
                            +
                            dependency.getArtifact()
                            +
                            "@"
                            +
                            dependency.getVersion()

            );




            Hash hash =
                    new Hash();



            hash.setAlg(
                    "SHA-256"
            );



            hash.setContent(

                    hashService.generate(
                            new File(
                                    dependency.getFile()
                            )
                    )

            );



            component.setHashes(
                    List.of(hash)
            );



            components.add(
                    component
            );

        }




        bom.setComponents(
                components
        );



        return bom;

    }





    private String getApplicationName(){


        return environment.getProperty(

                "spring.application.name",

                buildProperties != null
                        ?
                        buildProperties.getName()
                        :
                        "unknown"

        );


    }





    private String getVersion(){


        return buildProperties != null
                ?
                buildProperties.getVersion()
                :
                "unknown";


    }



}