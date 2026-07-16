package com.example.api_monitoring_starter.scanner;

import com.example.api_monitoring_starter.dto.ApiEndpointDTO;
import com.example.api_monitoring_starter.dto.ApiParameterDTO;
import com.example.api_monitoring_starter.dto.ControllerDTO;
import com.example.api_monitoring_starter.dto.ApiResponseDTO;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.*;
import java.util.*;


@Component
public class ApiScanner {


    private static final String STARTER_PACKAGE =
            "com.example.api_monitoring_starter";


    private final RequestMappingHandlerMapping handlerMapping;


    private final ObjectMapper objectMapper = new ObjectMapper();



    public ApiScanner(
            @Qualifier("requestMappingHandlerMapping")
            RequestMappingHandlerMapping handlerMapping) {

        this.handlerMapping = handlerMapping;
    }





    public List<ControllerDTO> scan() {


        Map<String,List<ApiEndpointDTO>> grouped =
                new LinkedHashMap<>();


        handlerMapping.getHandlerMethods()
                .forEach((mapping,handler)->{


                    String packageName =
                            handler.getBeanType()
                                    .getPackageName();



                    if(packageName.startsWith(STARTER_PACKAGE)){
                        return;
                    }



                    String controller =
                            handler.getBeanType()
                                    .getSimpleName();



                    String endpoint =
                            mapping.getPatternValues()
                                    .stream()
                                    .findFirst()
                                    .orElse("");



                    String httpMethod =
                            mapping.getMethodsCondition()
                                    .getMethods()
                                    .stream()
                                    .findFirst()
                                    .map(Enum::name)
                                    .orElse("REQUEST");



                    Method javaMethod =
                            handler.getMethod();



                    List<ApiParameterDTO> parameters =
                            new ArrayList<>();



                    for(Parameter parameter : javaMethod.getParameters()){


                        String parameterName =
                                parameter.getName();


                        String parameterType =
                                "Unknown";


                        boolean required=false;



                        if(parameter.isAnnotationPresent(RequestParam.class)){


                            RequestParam annotation =
                                    parameter.getAnnotation(RequestParam.class);


                            parameterType="RequestParam";

                            required =
                                    annotation.required();


                            if(!annotation.value().isEmpty()){
                                parameterName =
                                        annotation.value();
                            }

                        }



                        else if(parameter.isAnnotationPresent(PathVariable.class)){


                            PathVariable annotation =
                                    parameter.getAnnotation(PathVariable.class);


                            parameterType="PathVariable";

                            required=true;


                            if(!annotation.value().isEmpty()){
                                parameterName =
                                        annotation.value();
                            }

                        }



                        else if(parameter.isAnnotationPresent(RequestHeader.class)){


                            RequestHeader annotation =
                                    parameter.getAnnotation(RequestHeader.class);


                            parameterType="RequestHeader";

                            required =
                                    annotation.required();


                            if(!annotation.value().isEmpty()){
                                parameterName =
                                        annotation.value();
                            }

                        }



                        else if(parameter.isAnnotationPresent(RequestBody.class)){


                            RequestBody annotation =
                                    parameter.getAnnotation(RequestBody.class);


                            parameterType="RequestBody";

                            required =
                                    annotation.required();


                            parameterName =
                                    parameter.getType()
                                            .getSimpleName();

                        }



                        parameters.add(
                                new ApiParameterDTO(
                                        parameterName,
                                        parameterType,
                                        parameter.getType()
                                                .getSimpleName(),
                                        required
                                )
                        );


                    }



                    ApiResponseDTO response =
                            generateResponse(handler);



                    grouped.computeIfAbsent(
                                    controller,
                                    k->new ArrayList<>()
                            )
                            .add(

                                    new ApiEndpointDTO(
                                            httpMethod,
                                            endpoint,
                                            javaMethod.getName(),
                                            parameters,
                                            response
                                    )

                            );


                });



        return grouped.entrySet()
                .stream()
                .map(entry ->
                        new ControllerDTO(
                                entry.getKey(),
                                entry.getValue()
                        )
                )
                .toList();


    }







    private ApiResponseDTO generateResponse(
            HandlerMethod handlerMethod){


        Object example =
                generateResponseExample(handlerMethod);



        Object schema =
                generateResponseSchema(handlerMethod);



        return new ApiResponseDTO(

                200,

                "OK",

                "*/*",

                example,

                schema,

                List.of()

        );

    }







    private Object generateResponseExample(
            HandlerMethod handlerMethod){


        try{


            Method method =
                    handlerMethod.getMethod();



            Type returnType =
                    method.getGenericReturnType();



            // List<Student>
            if(returnType instanceof ParameterizedType type){


                Type actual =
                        type.getActualTypeArguments()[0];


                if(actual instanceof Class<?> clazz){


                    return List.of(
                            createExampleObject(clazz)
                    );

                }

            }



            // Student
            return createExampleObject(
                    method.getReturnType()
            );


        }
        catch(Exception e){

            return null;

        }

    }









    private Object generateResponseSchema(
            HandlerMethod handlerMethod){


        try{


            Method method =
                    handlerMethod.getMethod();



            Type returnType =
                    method.getGenericReturnType();



            Class<?> responseClass=null;



            // List<Student>
            if(returnType instanceof ParameterizedType type){


                Type actual =
                        type.getActualTypeArguments()[0];


                if(actual instanceof Class<?>){
                    responseClass=(Class<?>)actual;
                }

            }
            else{


                responseClass =
                        method.getReturnType();

            }





            Map<String,Object> schema =
                    new LinkedHashMap<>();


            schema.put(
                    "name",
                    responseClass.getSimpleName()
            );



            Map<String,String> fields =
                    new LinkedHashMap<>();



            for(Field field :
                    responseClass.getDeclaredFields()){



                String type =
                        field.getType()
                                .getSimpleName();



                if(type.equals("Long")){
                    type="integer int64";
                }
                else if(type.equals("Integer")){
                    type="integer";
                }
                else{
                    type="string";
                }



                fields.put(
                        field.getName(),
                        type
                );


            }



            schema.put(
                    "fields",
                    fields
            );



            return schema;


        }
        catch(Exception e){

            return null;

        }

    }







    private Object createExampleObject(
            Class<?> clazz){


        try{


            Object object =
                    clazz.getDeclaredConstructor()
                            .newInstance();



            Map<String,Object> response =
                    objectMapper.convertValue(
                            object,
                            Map.class
                    );



            response.replaceAll(
                    (key,value)->{


                        if(value instanceof Number){
                            return 0;
                        }


                        return "string";

                    }
            );



            return response;


        }
        catch(Exception e){

            return null;

        }

    }


}