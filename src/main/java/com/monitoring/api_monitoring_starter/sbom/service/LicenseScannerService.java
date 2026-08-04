package com.monitoring.api_monitoring_starter.sbom.service;


import com.monitoring.api_monitoring_starter.sbom.model.LicenseInfo;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


@Service
public class LicenseScannerService {


    public List<LicenseInfo> scanLicenses(JarFile jarFile) {

        List<LicenseInfo> licenses = new ArrayList<>();

        try {

            jarFile.stream()
                    .filter(entry ->
                            entry.getName()
                                    .startsWith("META-INF/maven/")
                                    &&
                                    entry.getName()
                                            .endsWith("pom.xml"))
                    .findFirst()
                    .ifPresent(entry -> {

                        try {

                            InputStream input =
                                    jarFile.getInputStream(entry);


                            Document document =
                                    DocumentBuilderFactory
                                            .newInstance()
                                            .newDocumentBuilder()
                                            .parse(input);



                            NodeList names =
                                    document
                                            .getElementsByTagName("name");


                            NodeList urls =
                                    document
                                            .getElementsByTagName("url");


                            if(names.getLength() > 0){

                                String name =
                                        names.item(0)
                                                .getTextContent();


                                String url = null;


                                if(urls.getLength() > 0){
                                    url =
                                            urls.item(0)
                                                    .getTextContent();
                                }



                                licenses.add(
                                        new LicenseInfo(
                                                name,
                                                url
                                        )
                                );
                            }


                        } catch(Exception e){

                        }

                    });


        } catch(Exception e){

        }


        return licenses;
    }

}