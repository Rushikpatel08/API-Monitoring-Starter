package com.monitoring.api_monitoring_starter.sbom.service;


import com.monitoring.api_monitoring_starter.sbom.model.SupplierInfo;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.jar.JarFile;



@Service
public class SupplierScannerService {


    public SupplierInfo scanSupplier(JarFile jarFile){


        try{


            return jarFile.stream()
                    .filter(entry ->
                            entry.getName()
                                    .startsWith("META-INF/maven/")
                                    &&
                                    entry.getName()
                                            .endsWith("pom.xml"))
                    .findFirst()
                    .map(entry -> {


                        try {


                            InputStream input =
                                    jarFile.getInputStream(entry);



                            Document document =
                                    DocumentBuilderFactory
                                            .newInstance()
                                            .newDocumentBuilder()
                                            .parse(input);



                            var organizations =
                                    document
                                            .getElementsByTagName(
                                                    "organization");



                            if(organizations
                                    .getLength()>0){


                                return new SupplierInfo(

                                        organizations
                                                .item(0)
                                                .getTextContent()

                                );

                            }


                        }catch(Exception ignored){

                        }


                        return null;


                    })
                    .orElse(null);



        }catch(Exception e){

            return null;
        }

    }


}