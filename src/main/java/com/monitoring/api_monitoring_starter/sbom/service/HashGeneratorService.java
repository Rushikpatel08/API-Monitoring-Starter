package com.monitoring.api_monitoring_starter.sbom.service;


import org.springframework.stereotype.Service;


import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;



@Service
public class HashGeneratorService {



    public String generate(
            File file
    ){


        try{


            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );



            byte[] content =
                    Files.readAllBytes(
                            file.toPath()
                    );



            byte[] hash =
                    digest.digest(
                            content
                    );



            StringBuilder result =
                    new StringBuilder();



            for(byte b : hash){


                result.append(
                        String.format(
                                "%02x",
                                b
                        )
                );

            }


            return result.toString();



        }catch(Exception e){

            throw new RuntimeException(e);

        }


    }


}