package com.example.api_monitoring_starter.exporter;

import com.example.api_monitoring_starter.Service.ApiRegistryService;
import com.example.api_monitoring_starter.dto.ApiEndpointDTO;
import com.example.api_monitoring_starter.dto.ControllerDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PostmanExportService {

    private final ObjectMapper objectMapper;
    private final ApiRegistryService apiRegistryService;

    public PostmanExportService(ObjectMapper objectMapper, ApiRegistryService apiRegistryService) {
        this.objectMapper = objectMapper;
        this.apiRegistryService = apiRegistryService;
    }

    public String generate(ApiEndpointDTO api) {
        try {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", api.getJavaMethod());
            item.put("request", buildRequest(api));
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(Map.of(
                    "info", Map.of("name", "Student API", "schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"),
                    "item", List.of(item)
            ));
        } catch (Exception e) {
            throw new RuntimeException("Failed creating Postman export", e);
        }
    }

    public String generateCollection() {
        try {
            List<Map<String, Object>> items = new java.util.ArrayList<>();
            for (ControllerDTO controller : apiRegistryService.getApis()) {
                for (ApiEndpointDTO api : controller.getApis()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("name", api.getJavaMethod());
                    item.put("request", buildRequest(api));
                    items.add(item);
                }
            }
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(Map.of(
                    "info", Map.of("name", "Student API", "schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"),
                    "item", items
            ));
        } catch (Exception e) {
            throw new RuntimeException("Failed creating Postman collection", e);
        }
    }

    private Map<String, Object> buildRequest(ApiEndpointDTO api) throws Exception {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("method", api.getHttpMethod().toUpperCase());
        request.put("header", List.of());

        if (api.getRequest() != null) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("mode", "raw");
            body.put("raw", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(api.getRequest().getExample()));
            request.put("body", body);
        } else {
            request.put("body", null);
        }

        Map<String, Object> url = new LinkedHashMap<>();
        url.put("raw", "{{baseUrl}}" + api.getEndpoint());
        request.put("url", url);
        return request;
    }
}
