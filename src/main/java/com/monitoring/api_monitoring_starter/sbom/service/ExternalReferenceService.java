package com.monitoring.api_monitoring_starter.sbom.service;


import com.monitoring.api_monitoring_starter.sbom.model.ExternalReference;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;



@Service
public class ExternalReferenceService {



    public List<ExternalReference> scanReferences(
            JarFile jarFile){


        List<ExternalReference> references =
                new ArrayList<>();


        try{


            jarFile.stream()

                    .filter(entry ->
                            entry.getName()
                                    .startsWith("META-INF/maven/")
                                    &&
                                    entry.getName()
                                            .endsWith("pom.xml"))

                    .findFirst()

                    .ifPresent(entry ->{


                        try{


                            InputStream input =
                                    jarFile.getInputStream(entry);



                            Document document =
                                    DocumentBuilderFactory
                                            .newInstance()
                                            .newDocumentBuilder()
                                            .parse(input);



                            addReference(
                                    references,
                                    "website",
                                    document,
                                    "url"
                            );


                            addReference(
                                    references,
                                    "scm",
                                    document,
                                    "connection"
                            );



                        }catch(Exception ignored){

                        }


                    });



        }catch(Exception ignored){



        }


        return references;

    }




    private void addReference(

            List<ExternalReference> list,
            String type,
            Document document,
            String tag){


        var nodes =
                document.getElementsByTagName(tag);



        if(nodes.getLength()>0){


            String value =
                    nodes.item(0)
                            .getTextContent();



            if(value!=null &&
                    !value.isBlank()){


                list.add(
                        new ExternalReference(
                                type,
                                value
                        )
                );

            }

        }


    }



}