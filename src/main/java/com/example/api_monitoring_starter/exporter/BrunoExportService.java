package com.example.api_monitoring_starter.exporter;


import com.example.api_monitoring_starter.Service.OpenApiExportService;
import com.example.api_monitoring_starter.dto.ApiEndpointDTO;
import com.example.api_monitoring_starter.dto.ControllerDTO;
import com.example.api_monitoring_starter.Service.ApiRegistryService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Service
public class BrunoExportService {


    private final ObjectMapper objectMapper;

    private final ApiRegistryService apiRegistryService;



    public BrunoExportService(
            ObjectMapper objectMapper,
            ApiRegistryService apiRegistryService
    ){

        this.objectMapper = objectMapper;
        this.apiRegistryService = apiRegistryService;

    }



    public String generate(ApiEndpointDTO api) {


        StringBuilder bruno = new StringBuilder();


        bruno.append("meta {\n");
        bruno.append("  name: ")
                .append(api.getJavaMethod())
                .append("\n");
        bruno.append("  type: http\n");
        bruno.append("  seq: 1\n");
        bruno.append("}\n\n");


        bruno.append("request {\n");

        bruno.append("  url: {{baseUrl}}")
                .append(api.getEndpoint())
                .append("\n");


        bruno.append("  method: ")
                .append(api.getHttpMethod())
                .append("\n");


        if(supportsRequestBody(api.getHttpMethod())
                && api.getRequest()!=null){

            bruno.append("  body: json\n");

        }


        bruno.append("}\n\n");



        bruno.append("headers {\n");
        bruno.append("  Content-Type: application/json\n");
        bruno.append("}\n");



        if(supportsRequestBody(api.getHttpMethod())
                && api.getRequest()!=null){


            bruno.append("\nbody:json {\n");


            try {

                String json =
                        objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(
                                        api.getRequest().getExample()
                                );


                bruno.append(json);

            }
            catch(Exception e){

                bruno.append("{}");

            }


            bruno.append("\n}\n");

        }


        return bruno.toString();

    }





    private boolean supportsRequestBody(String method){

        return method.equalsIgnoreCase("POST")
                ||
                method.equalsIgnoreCase("PUT")
                ||
                method.equalsIgnoreCase("PATCH");

    }






    public byte[] generateCollection(){


        try(
                ByteArrayOutputStream baos =
                        new ByteArrayOutputStream();

                ZipOutputStream zip =
                        new ZipOutputStream(baos)

        ){



            Set<String> generatedFiles = new HashSet<>();



            // ============================
            // Bruno Config
            // ============================

            addEntry(
                    zip,
                    generatedFiles,
                    "Student-API-Bruno/bruno.json",
                    """
                    {
                      "version": "1",
                      "name": "Student API",
                      "type": "collection"
                    }
                    """
            );




            // ============================
            // Environment
            // ============================

            addEntry(
                    zip,
                    generatedFiles,
                    "Student-API-Bruno/environments/local.bru",
                    """
                    vars {
                      baseUrl: http://localhost:3000/api/v1
                    }
                    """
            );






            // ============================
            // API Requests
            // ============================


            List<ControllerDTO> controllers =
                    apiRegistryService.getApis();



            for(ControllerDTO controller : controllers){


                for(ApiEndpointDTO api :
                        controller.getApis()){



                    String fileName =
                            "Student-API-Bruno/requests/"
                                    + createFileName(api)
                                    + ".bru";



                    // Prevent duplicate files
                    if(generatedFiles.contains(fileName)){

                        continue;

                    }



                    addEntry(
                            zip,
                            generatedFiles,
                            fileName,
                            generate(api)
                    );

                }

            }




            zip.finish();


            return baos.toByteArray();


        }
        catch(Exception e){

            throw new RuntimeException(
                    "Failed creating Bruno collection",
                    e
            );

        }

    }






    private String createFileName(ApiEndpointDTO api){


        String name =
                api.getJavaMethod()
                        + "_"
                        + api.getHttpMethod()
                        + "_"
                        + api.getEndpoint();



        return name
                .replace("/", "_")
                .replace("{","")
                .replace("}","")
                .replace(":","")
                .replace(" ","_");

    }







    private void addEntry(
            ZipOutputStream zip,
            Set<String> generatedFiles,
            String fileName,
            String content
    )
            throws Exception {



        if(generatedFiles.contains(fileName)){

            return;

        }



        generatedFiles.add(fileName);



        zip.putNextEntry(
                new ZipEntry(fileName)
        );



        zip.write(
                content.getBytes(StandardCharsets.UTF_8)
        );


        zip.closeEntry();

    }

}