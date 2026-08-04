package com.monitoring.api_monitoring_starter.sbom.service;


import com.monitoring.api_monitoring_starter.sbom.model.DependencyInfo;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.*;
import java.util.jar.*;
import java.util.jar.JarEntry;


@Service
public class DependencyScannerService {


    public List<DependencyInfo> scanDependencies() {


        Map<String, DependencyInfo> dependencyMap =
                new HashMap<>();


        try {


            // 1. Scan normal classpath
            scanClassPathDependencies(dependencyMap);


            // 2. Scan Spring Boot executable jar
            scanSpringBootJar(dependencyMap);



        } catch(Exception e){

            e.printStackTrace();

        }


        return new ArrayList<>(
                dependencyMap.values()
        );

    }





    private void scanClassPathDependencies(
            Map<String,DependencyInfo> dependencies
    )
            throws Exception {


        String classPath =
                System.getProperty(
                        "java.class.path"
                );


        String[] entries =
                classPath.split(
                        File.pathSeparator
                );


        for(String entry : entries){


            File file =
                    new File(entry);


            if(file.isFile()
                    &&
                    file.getName()
                            .endsWith(".jar")){


                DependencyInfo info =
                        readPomProperties(file);


                if(info != null){

                    dependencies.put(
                            info.getGroup()
                                    + ":"
                                    + info.getArtifact()
                                    + ":"
                                    + info.getVersion(),
                            info
                    );

                }

            }

        }

    }








    private void scanSpringBootJar(
            Map<String,DependencyInfo> dependencies
    )
            throws Exception {


        String location =
                DependencyScannerService.class
                        .getProtectionDomain()
                        .getCodeSource()
                        .getLocation()
                        .getPath();


        File applicationJar =
                new File(location);


        if(!applicationJar.getName()
                .endsWith(".jar")){

            return;
        }




        try(JarFile jar =
                    new JarFile(applicationJar)){



            Enumeration<JarEntry> entries =
                    jar.entries();



            while(entries.hasMoreElements()){


                JarEntry entry =
                        entries.nextElement();



                if(entry.getName()
                        .startsWith(
                                "BOOT-INF/lib/"
                        )
                        &&
                        entry.getName()
                                .endsWith(".jar")){


                    String nestedJarName =
                            entry.getName()
                                    .replace(
                                            "BOOT-INF/lib/",
                                            ""
                                    );



                    String[] parts =
                            nestedJarName
                                    .replace(
                                            ".jar",
                                            ""
                                    )
                                    .split("-");



                    if(parts.length >= 2){


                        String version =
                                parts[parts.length-1];


                        String artifact =
                                nestedJarName.substring(
                                        0,
                                        nestedJarName.lastIndexOf(
                                                "-" + version
                                        )
                                );



                        DependencyInfo info =
                                new DependencyInfo(
                                        "unknown",
                                        artifact,
                                        version,
                                        nestedJarName,
                                        calculateHash(applicationJar)
                                );

                        dependencies.put(
                                info.getGroup()
                                        + ":"
                                        + info.getArtifact()
                                        + ":"
                                        + info.getVersion(),
                                info
                        );

                    }

                }

            }

        }

    }








    private DependencyInfo readPomProperties(
            File jarFile
    ){


        try(JarFile jar =
                    new JarFile(jarFile)){



            Enumeration<JarEntry> entries =
                    jar.entries();



            while(entries.hasMoreElements()){


                JarEntry entry =
                        entries.nextElement();



                if(entry.getName()
                        .startsWith(
                                "META-INF/maven/"
                        )
                        &&
                        entry.getName()
                                .endsWith(
                                        "pom.properties"
                                )){


                    Properties properties =
                            new Properties();



                    try(InputStream input =
                                jar.getInputStream(entry)){


                        properties.load(input);

                    }



                    return new DependencyInfo(

                            properties.getProperty(
                                    "groupId"
                            ),

                            properties.getProperty(
                                    "artifactId"
                            ),

                            properties.getProperty(
                                    "version"
                            ),

                            jarFile.getAbsolutePath(),

                            calculateHash(
                                    jarFile
                            )

                    );

                }

            }



        }catch(Exception ignored){}


        return null;

    }








    private String calculateHash(
            File file
    ){


        try{


            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );


            byte[] bytes =
                    Files.readAllBytes(
                            file.toPath()
                    );


            byte[] hash =
                    digest.digest(bytes);



            StringBuilder result =
                    new StringBuilder();



            for(byte b:hash){

                result.append(
                        String.format(
                                "%02x",
                                b
                        )
                );

            }


            return result.toString();


        }catch(Exception e){

            return null;

        }

    }



}