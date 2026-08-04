package com.monitoring.api_monitoring_starter.scanner;

import com.monitoring.api_monitoring_starter.dto.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.monitoring.api_monitoring_starter.dto.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import io.swagger.v3.oas.annotations.Operation;

import java.lang.reflect.*;
import java.util.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
@Component
public class ApiScanner {

    /**
     * Packages to exclude from API scanning
     * Can be customized via application.properties
     */
    @Value("${api.monitoring.exclude-packages:org.springframework,org.springframework.boot}")
    private String excludePackagesConfig;

    /**
     * Whether to include internal starter packages
     * Default is false (excludes internal packages)
     */
    @Value("${api.monitoring.include-internal:false}")
    private boolean includeInternalPackages;

    private final RequestMappingHandlerMapping handlerMapping;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Set<String> excludePackages;

    public ApiScanner(
            @Qualifier("requestMappingHandlerMapping")
            RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    /**
     * Initialize exclude packages set
     */
    @PostConstruct
    private void initializeExcludePackages() {
        this.excludePackages = new HashSet<>();

        // Add default internal starter package if not explicitly included
        if (!includeInternalPackages) {
            this.excludePackages.add("com.example.api_monitoring_starter");
        }

        // Parse configured exclude packages
        if (excludePackagesConfig != null && !excludePackagesConfig.isEmpty()) {
            String[] packages = excludePackagesConfig.split(",");
            for (String pkg : packages) {
                String trimmed = pkg.trim();
                if (!trimmed.isEmpty()) {
                    this.excludePackages.add(trimmed);
                }
            }
        }
    }

    /**
     * Check if a package should be excluded from scanning
     */
    private boolean isExcludedPackage(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return false;
        }

        return excludePackages.stream()
                .anyMatch(pkg -> packageName.startsWith(pkg));
    }

    public List<ControllerDTO> scan() {

        Map<String, List<ApiEndpointDTO>> grouped = new LinkedHashMap<>();

        Map<String, String> tagNames = new LinkedHashMap<>();
        Map<String, String> tagDescriptions = new LinkedHashMap<>();

        handlerMapping.getHandlerMethods().forEach((mapping, handler) -> {

            try {
                String packageName = handler.getBeanType().getPackageName();

                // Skip excluded packages dynamically
                if (isExcludedPackage(packageName)) {
                    return;
                }
                String apiType =
                        determineApiType(handler.getBeanType());

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

                        if(!annotation.value().isEmpty()) {
                            parameterName = annotation.value();
                        }

                    }

                    // Skip framework-specific parameters
                    if (!parameterType.equals("Unknown") &&
                            !isFrameworkParameter(parameterName, parameterType)) {

                        ApiParameterDTO param = new ApiParameterDTO(
                                parameterName,
                                parameterType,
                                parameter.getType().getSimpleName(),
                                required
                        );

                        parameters.add(param);
                    }
                }

                // ===========================================
                // Response
                // ===========================================

                ApiResponseDTO response = extractResponse(javaMethod);

                ApiEndpointDTO endpoint_dto = new ApiEndpointDTO(
                        apiId,
                        httpMethod,
                        endpoint,
                        javaMethod.getName(),
                        parameters,
                        response,
                        request,
                        authentication,
                        summary,
                        description,
                        apiType

                );

                grouped.computeIfAbsent(controller, k -> new ArrayList<>())
                        .add(endpoint_dto);

            } catch (Exception e) {
                System.err.println("Error scanning endpoint: " + e.getMessage());
                e.printStackTrace();
            }

        });

        List<ControllerDTO> controllers = new ArrayList<>();

        grouped.forEach((controllerName, apis) -> {
            ControllerDTO dto = new ControllerDTO(
                    controllerName,
                    tagNames.getOrDefault(controllerName, controllerName),
                    tagDescriptions.getOrDefault(controllerName, ""),
                    apis
            );
            controllers.add(dto);
        });

        return controllers;
    }

    /**
     * Check if a parameter is a framework-specific parameter
     */
    private boolean isFrameworkParameter(String parameterName, String parameterType) {
        return parameterType.equals("Unknown") ||
                parameterName.equals("request") ||
                parameterName.equals("response") ||
                parameterName.equals("principal");
    }

    /**
     * Extract response information from method
     */
    private ApiResponseDTO extractResponse(Method method) {
        return new ApiResponseDTO(
                200,
                "OK",
                "application/json",
                new LinkedHashMap<>(),
                null,
                null
        );
    }

    /**
     * Create example object for a class
     */
    private Object createExampleObject(Class<?> clazz) {
        try {
            Map<String, Object> response = new LinkedHashMap<>();

            for (Field field : clazz.getDeclaredFields()) {
                Class<?> type = field.getType();

                if (type == String.class) {
                    response.put(field.getName(), "string");
                } else if (type == Long.class || type == long.class) {
                    response.put(field.getName(), 0L);
                } else if (type == Integer.class || type == int.class) {
                    response.put(field.getName(), 0);
                } else if (type == Double.class || type == double.class) {
                    response.put(field.getName(), 0.0);
                } else if (type == Float.class || type == float.class) {
                    response.put(field.getName(), 0.0f);
                } else if (type == Boolean.class || type == boolean.class) {
                    response.put(field.getName(), false);
                } else if (type == List.class) {
                    response.put(field.getName(), new ArrayList<>());
                } else {
                    response.put(field.getName(), "object");
                }
            }

            return response;
        } catch (Exception e) {
            return null;
        }
    }
    private String determineApiType(Class<?> controllerClass) {

        String packageName =
                controllerClass.getPackageName();

        // Framework / third-party APIs
        if (packageName.startsWith("org.springframework")
                || packageName.startsWith("org.springdoc")
                || packageName.startsWith("org.webjars")
                || packageName.startsWith("io.swagger")
                || packageName.startsWith("jakarta")
                || packageName.startsWith("javax")) {

            return "SYSTEM";
        }

        // Application APIs
        return "APPLICATION";
    }
    /**
     * Generate schema for a class
     */
    private Map<String, Object> generateSchema(Class<?> clazz) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new LinkedHashMap<>();

        for (Field field : clazz.getDeclaredFields()) {
            Map<String, Object> property = new LinkedHashMap<>();
            Class<?> type = field.getType();

            if (type == String.class) {
                property.put("type", "string");
            } else if (type == Long.class || type == long.class ||
                    type == Integer.class || type == int.class) {
                property.put("type", "integer");
            } else if (type == Double.class || type == double.class ||
                    type == Float.class || type == float.class) {
                property.put("type", "number");
            } else if (type == Boolean.class || type == boolean.class) {
                property.put("type", "boolean");
            } else {
                property.put("type", "object");
            }

            properties.put(field.getName(), property);
        }

        schema.put("properties", properties);
        return schema;
    }
}