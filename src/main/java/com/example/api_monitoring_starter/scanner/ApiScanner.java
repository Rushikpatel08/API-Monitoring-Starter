package com.example.api_monitoring_starter.scanner;

import com.example.api_monitoring_starter.dto.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import io.swagger.v3.oas.annotations.Operation;
import java.lang.reflect.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import io.swagger.v3.oas.annotations.tags.Tag;

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

        Map<String, List<ApiEndpointDTO>> grouped = new LinkedHashMap<>();

        Map<String, String> tagNames = new LinkedHashMap<>();
        Map<String, String> tagDescriptions = new LinkedHashMap<>();

        handlerMapping.getHandlerMethods().forEach((mapping, handler) -> {

            String packageName = handler.getBeanType().getPackageName();

            if (packageName.startsWith(STARTER_PACKAGE)) {
                return;
            }

            String controller = handler.getBeanType().getSimpleName();

            // ===========================================
            // Controller Tag
            // ===========================================

            Tag tag = handler.getBeanType().getAnnotation(Tag.class);

            if (tag != null) {
                tagNames.put(controller, tag.name());
                tagDescriptions.put(controller, tag.description());
            } else {
                tagNames.put(controller, controller);
                tagDescriptions.put(controller, "");
            }

            // ===========================================
            // Endpoint
            // ===========================================

            String endpoint = mapping.getPatternValues()
                    .stream()
                    .findFirst()
                    .orElse("");

            String httpMethod = mapping.getMethodsCondition()
                    .getMethods()
                    .stream()
                    .findFirst()
                    .map(Enum::name)
                    .orElse("REQUEST");

            Method javaMethod = handler.getMethod();

            // ===========================================
            // Swagger Operation
            // ===========================================

            String summary = "";
            String description = "";

            Operation operation =
                    javaMethod.getAnnotation(Operation.class);

            if (operation != null) {
                summary = operation.summary();
                description = operation.description();
            }

            // ===========================================
            // Parameters
            // ===========================================

            List<ApiParameterDTO> parameters = new ArrayList<>();

            ApiRequestDTO request = null;

            for (Parameter parameter : javaMethod.getParameters()) {

                String parameterName = parameter.getName();
                String parameterType = "Unknown";
                boolean required = false;

                if (parameter.isAnnotationPresent(RequestParam.class)) {

                    RequestParam annotation =
                            parameter.getAnnotation(RequestParam.class);

                    parameterType = "RequestParam";
                    required = annotation.required();

                    if (!annotation.value().isEmpty()) {
                        parameterName = annotation.value();
                    }
                }

                else if (parameter.isAnnotationPresent(PathVariable.class)) {

                    PathVariable annotation =
                            parameter.getAnnotation(PathVariable.class);

                    parameterType = "PathVariable";
                    required = true;

                    if (!annotation.value().isEmpty()) {
                        parameterName = annotation.value();
                    }
                }

                else if (parameter.isAnnotationPresent(RequestHeader.class)) {

                    RequestHeader annotation =
                            parameter.getAnnotation(RequestHeader.class);

                    parameterType = "RequestHeader";
                    required = annotation.required();

                    if (!annotation.value().isEmpty()) {
                        parameterName = annotation.value();
                    }
                }

                else if (parameter.isAnnotationPresent(RequestBody.class)) {

                    RequestBody annotation =
                            parameter.getAnnotation(RequestBody.class);

                    parameterType = "RequestBody";
                    required = annotation.required();

                    parameterName = parameter.getType().getSimpleName();

                    Class<?> requestClass = parameter.getType();

                    request = new ApiRequestDTO(
                            "application/json",
                            createExampleObject(requestClass),
                            generateSchema(requestClass)
                    );
                }

                parameters.add(
                        new ApiParameterDTO(
                                parameterName,
                                parameterType,
                                parameter.getType().getSimpleName(),
                                required
                        )
                );
            }

            // ===========================================
            // Response
            // ===========================================

            ApiResponseDTO response =
                    generateResponse(handler);

            grouped.computeIfAbsent(
                            controller,
                            k -> new ArrayList<>())
                    .add(
                            new ApiEndpointDTO(
                                    httpMethod,
                                    endpoint,
                                    javaMethod.getName(),
                                    parameters,
                                    response,
                                    request,
                                    summary,
                                    description
                            )
                    );

        });

        // ===========================================
        // Build ControllerDTO list
        // ===========================================

        List<ControllerDTO> controllers = new ArrayList<>();

        grouped.forEach((controller, apis) -> {

            controllers.add(
                    new ControllerDTO(
                            controller,
                            tagNames.get(controller),
                            tagDescriptions.get(controller),
                            apis
                    )
            );

        });

        return controllers;
    }



    private Object generateSchema(Class<?> clazz){

        Map<String,Object> schema =
                new LinkedHashMap<>();


        schema.put(
                "name",
                clazz.getSimpleName()
        );


        Map<String,String> fields =
                new LinkedHashMap<>();


        for(Field field : clazz.getDeclaredFields()){

            fields.put(
                    field.getName(),
                    resolveSchemaType(field.getType())
            );

        }


        schema.put(
                "fields",
                fields
        );


        return schema;
    }



    private ApiResponseDTO generateResponse(
            HandlerMethod handlerMethod){

        Object example =
                generateResponseExample(handlerMethod);

        Object schema =
                generateResponseSchema(handlerMethod);


        ResponseStatus responseStatus =
                handlerMethod.getMethodAnnotation(ResponseStatus.class);


        int statusCode = 200;
        String description = "OK";


        // Check @ResponseStatus
        if(responseStatus != null){

            statusCode = responseStatus.code().value();
            description = responseStatus.code().getReasonPhrase();

        }


        return new ApiResponseDTO(
                statusCode,
                description,
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



                String type = resolveSchemaType(field.getType());



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



    private String resolveSchemaType(Class<?> fieldType) {

        if (fieldType == String.class) {
            return "string";
        }

        if (fieldType == Long.class || fieldType == long.class) {
            return "integer int64";
        }

        if (fieldType == Integer.class || fieldType == int.class) {
            return "integer int32";
        }

        if (fieldType == Double.class || fieldType == double.class) {
            return "number double";
        }

        if (fieldType == Float.class || fieldType == float.class) {
            return "number float";
        }

        if (fieldType == Boolean.class || fieldType == boolean.class) {
            return "boolean";
        }

        // Dynamic date/time handling
        if (java.time.temporal.Temporal.class.isAssignableFrom(fieldType)) {
            return "string date-time";
        }

        return "object";
    }



    private Object createExampleObject(Class<?> clazz) {

        try {

            Map<String, Object> response = new LinkedHashMap<>();

            for (Field field : clazz.getDeclaredFields()) {

                Class<?> type = field.getType();

                if (type == String.class) {
                    response.put(field.getName(), "string");
                }
                else if (type == Long.class || type == long.class) {
                    response.put(field.getName(), 0);
                }
                else if (type == Integer.class || type == int.class) {
                    response.put(field.getName(), 0);
                }
                else if (type == Double.class || type == double.class) {
                    response.put(field.getName(), 0.0);
                }
                else if (type == Float.class || type == float.class) {
                    response.put(field.getName(), 0.0);
                }
                else if (type == Boolean.class || type == boolean.class) {
                    response.put(field.getName(), false);
                }
                else if (java.time.temporal.Temporal.class.isAssignableFrom(type)) {
                    String currentTime = LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                    response.put(field.getName(), currentTime);
                }
                else {
                    response.put(field.getName(), null);
                }
            }

            return response;

        } catch (Exception e) {
            return null;
        }
    }




}