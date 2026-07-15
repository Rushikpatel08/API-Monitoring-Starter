package com.example.api_monitoring_starter.scanner;

import com.example.api_monitoring_starter.dto.ApiControllerDTO;
import com.example.api_monitoring_starter.dto.ApiEndpointDTO;
import com.example.api_monitoring_starter.dto.ApiParameterDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ApiScanner {

    private final RequestMappingHandlerMapping handlerMapping;

    public ApiScanner(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    public List<ApiControllerDTO> scan() {
        Map<String, List<ApiEndpointDTO>> grouped = new LinkedHashMap<>();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMapping.getHandlerMethods().entrySet()) {
            HandlerMethod handler = entry.getValue();
            String controllerName = handler.getBeanType().getSimpleName();
            String path = entry.getKey().getDirectPaths().stream().findFirst().orElse("/");
            String httpMethod = entry.getKey().getMethodsCondition().getMethods().stream().findFirst().map(Enum::name).orElse("GET");
            String summary = buildSummary(handler.getMethod());
            String returnType = handler.getMethod().getReturnType().getSimpleName();
            List<ApiParameterDTO> params = buildParams(handler.getMethod());

            grouped.computeIfAbsent(controllerName, key -> new ArrayList<>())
                    .add(new ApiEndpointDTO(httpMethod, path, summary, params, returnType));
        }

        List<ApiControllerDTO> result = new ArrayList<>();
        for (Map.Entry<String, List<ApiEndpointDTO>> entry : grouped.entrySet()) {
            result.add(new ApiControllerDTO(entry.getKey(), "Auto-detected controller", entry.getValue()));
        }

        return result;
    }

    private String buildSummary(Method method) {
        String name = method.getName();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1).replaceAll("([a-z])([A-Z])", "$1 $2");
    }

    private List<ApiParameterDTO> buildParams(Method method) {
        List<ApiParameterDTO> params = new ArrayList<>();
        for (java.lang.reflect.Parameter parameter : method.getParameters()) {
            params.add(new ApiParameterDTO(parameter.getName(), parameter.getType().getSimpleName(), false));
        }
        return params;
    }
}
