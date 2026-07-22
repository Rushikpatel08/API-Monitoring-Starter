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
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Principal;
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

                    String packageName =
                            handler.getBeanType()
                                    .getPackageName();


                    if (packageName.startsWith(STARTER_PACKAGE)
                            ||
                            packageName.startsWith("org.springframework")) {

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

                    String apiId =
                            controller
                                    + "_"
                                    + httpMethod
                                    + "_"
                                    + endpoint
                                    .replace("/", "_")
                                    .replace("{", "")
                                    .replace("}", "");
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


// ================================
// Authentication
// ================================

                    ApiAuthDTO authentication = null;

            for (Parameter parameter : javaMethod.getParameters()) {


                String parameterName = parameter.getName();
                String parameterType = "Unknown";
                boolean required = false;



                // ==============================
                // Request Param
                // ==============================

                if(parameter.isAnnotationPresent(RequestParam.class)) {

                    RequestParam annotation =
                            parameter.getAnnotation(RequestParam.class);

                    parameterType = "RequestParam";
                    required = annotation.required();

                    if(!annotation.value().isEmpty()) {
                        parameterName = annotation.value();
                    }

                }



                // ==============================
                // Path Variable
                // ==============================

                else if(parameter.isAnnotationPresent(PathVariable.class)) {

                    PathVariable annotation =
                            parameter.getAnnotation(PathVariable.class);

                    parameterType = "PathVariable";
                    required = true;

                    if(!annotation.value().isEmpty()) {
                        parameterName = annotation.value();
                    }

                }



                // ==============================
                // Request Header
                // ==============================

                else if(parameter.isAnnotationPresent(RequestHeader.class)) {


                    RequestHeader annotation =
                            parameter.getAnnotation(RequestHeader.class);


                    parameterType = "RequestHeader";

                    required = annotation.required();


                    if(!annotation.value().isEmpty()) {
                        parameterName = annotation.value();
                    }


                    if(parameterName.equalsIgnoreCase("Authorization")) {

                        authentication =
                                new ApiAuthDTO(
                                        "Bearer",
                                        "Authorization",
                                        "token"
                                );

                    }

                    else if(parameterName.equalsIgnoreCase("x-api-key")
                            ||
                            parameterName.equalsIgnoreCase("api-key")) {


                        authentication =
                                new ApiAuthDTO(
                                        "API_KEY",
                                        parameterName,
                                        "apiKey"
                                );

                    }

                }



                // ==============================
                // Request Body
                // ==============================

                else if(parameter.isAnnotationPresent(RequestBody.class)) {


                    RequestBody annotation =
                            parameter.getAnnotation(RequestBody.class);


                    parameterType = "RequestBody";

                    required = annotation.required();


                    parameterName =
                            parameter.getType().getSimpleName();


                    Class<?> requestClass =
                            parameter.getType();


                    request =
                            new ApiRequestDTO(
                                    "application/json",
                                    createExampleObject(requestClass),
                                    generateSchema(requestClass)
                            );

                }



                // ==============================
                // Model Attribute
                // ==============================

                else if(parameter.isAnnotationPresent(ModelAttribute.class)) {


                    parameterType = "ModelAttribute";

                    required = true;


                    parameterName =
                            parameter.getType().getSimpleName();


                    request =
                            new ApiRequestDTO(
                                    "application/x-www-form-urlencoded",
                                    createExampleObject(parameter.getType()),
                                    generateSchema(parameter.getType())
                            );

                }



                // ==============================
                // Request Part (File Upload)
                // ==============================

                else if(parameter.isAnnotationPresent(RequestPart.class)) {


                    RequestPart annotation =
                            parameter.getAnnotation(RequestPart.class);


                    parameterType = "RequestPart";

                    required = annotation.required();


                    if(!annotation.value().isEmpty()) {
                        parameterName = annotation.value();
                    }

                }



                // ==============================
                // Cookie Value
                // ==============================

                else if(parameter.isAnnotationPresent(CookieValue.class)) {


                    CookieValue annotation =
                            parameter.getAnnotation(CookieValue.class);


                    parameterType = "CookieValue";

                    required = annotation.required();


                    if(!annotation.value().isEmpty()) {
                        parameterName = annotation.value();
                    }

                }



                // ==============================
                // Matrix Variable
                // ==============================

                else if(parameter.isAnnotationPresent(MatrixVariable.class)) {


                    MatrixVariable annotation =
                            parameter.getAnnotation(MatrixVariable.class);


                    parameterType = "MatrixVariable";

                    required = annotation.required();


                    if(!annotation.value().isEmpty()) {
                        parameterName = annotation.value();
                    }

                }



                // ==============================
                // Session Attribute
                // ==============================

                else if(parameter.isAnnotationPresent(SessionAttribute.class)) {


                    SessionAttribute annotation =
                            parameter.getAnnotation(SessionAttribute.class);


                    parameterType = "SessionAttribute";

                    required = annotation.required();


                    parameterName = annotation.value();

                }



                // ==============================
                // Request Attribute
                // ==============================

                else if(parameter.isAnnotationPresent(RequestAttribute.class)) {


                    RequestAttribute annotation =
                            parameter.getAnnotation(RequestAttribute.class);


                    parameterType = "RequestAttribute";

                    required = annotation.required();


                    parameterName = annotation.value();

                }



                // ==============================
                // Principal
                // ==============================

                else if(parameter.getType()
                        .equals(Principal.class)) {


                    parameterType = "Principal";

                    parameterName = "principal";

                }



                // ==============================
                // HttpServletRequest
                // ==============================

                else if(parameter.getType()
                        .equals(HttpServletRequest.class)) {


                    parameterType = "HttpServletRequest";

                    parameterName = "request";

                }



                // ==============================
                // HttpServletResponse
                // ==============================

                else if(parameter.getType()
                        .equals(HttpServletResponse.class)) {


                    parameterType = "HttpServletResponse";

                    parameterName = "response";

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

                    ApiEndpointDTO api =
                            new ApiEndpointDTO(

                                    apiId,

                                    httpMethod,

                                    endpoint,

                                    javaMethod.getName(),

                                    parameters,

                                    response,

                                    request,

                                    authentication,

                                    summary,

                                    description

                            );


// Set API category
                    api.setApiType(
                            classifyApi(endpoint)
                    );


// Add API
                    grouped.computeIfAbsent(
                                    controller,
                                    k -> new ArrayList<>()
                            )
                            .add(api);
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




    private String classifyApi(String endpoint){


        List<String> systemApis = List.of(

                "/v3",
                "/swagger",
                "/actuator",
                "/error",
                "/metadata",
                "/monitoring",
                "/webjars"

        );


        for(String system : systemApis){

            if(endpoint.startsWith(system)){

                return "SYSTEM";

            }

        }


        return "APPLICATION";

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