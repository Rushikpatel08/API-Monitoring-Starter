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
public class InsomniaExportService {

    private final ObjectMapper objectMapper;
    private final ApiRegistryService apiRegistryService;

    public InsomniaExportService(ObjectMapper objectMapper, ApiRegistryService apiRegistryService) {
        this.objectMapper = objectMapper;
        this.apiRegistryService = apiRegistryService;
    }

    public String generate(ApiEndpointDTO api) {
        try {
            Map<String, Object> document = new LinkedHashMap<>();
            document.put("_type", "export");
            document.put("__export_format", 4);
            document.put("__export_date", java.time.Instant.now().toString());
            document.put("__export_source", "api-monitoring-starter");

            Map<String, Object> resource = new LinkedHashMap<>();
            resource.put("_id", api.getId());
            resource.put("_type", "request");
            resource.put("parentId", "wrk_1");
            resource.put("name", api.getJavaMethod());
            resource.put("method", api.getHttpMethod().toUpperCase());
            resource.put("url", "{{ base_url }}" + api.getEndpoint());
            resource.put("description", api.getDescription());
            resource.put("body", Map.of(
                    "mimeType", api.getRequest() == null ? "application/json" : api.getRequest().getMediaType(),
                    "text", api.getRequest() == null ? "" : objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(api.getRequest().getExample())
            ));
            resource.put("requestPayload", "payload");

            if (api.getRequest() != null) {
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("mimeType", api.getRequest().getMediaType());
                body.put("text", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(api.getRequest().getExample()));
                resource.put("body", body);
            }

            document.put("resources", List.of(resource));
            return serialize(document);
        } catch (Exception e) {
            throw new RuntimeException("Failed creating Insomnia export", e);
        }
    }

    public String generateCollection() {
        try {
            Map<String, Object> document = new LinkedHashMap<>();
            document.put("_type", "export");
            document.put("__export_format", 4);
            document.put("__export_date", java.time.Instant.now().toString());
            document.put("__export_source", "api-monitoring-starter");

            List<Map<String, Object>> resources = new java.util.ArrayList<>();
            Map<String, Object> workspace = new LinkedHashMap<>();
            workspace.put("_id", "wrk_1");
            workspace.put("_type", "workspace");
            workspace.put("name", "Student API");
            resources.add(workspace);

            for (ControllerDTO controller : apiRegistryService.getApis()) {
                for (ApiEndpointDTO api : controller.getApis()) {
                    Map<String, Object> resource = new LinkedHashMap<>();
                    resource.put("_id", api.getId());
                    resource.put("_type", "request");
                    resource.put("parentId", "wrk_1");
                    resource.put("name", api.getJavaMethod());
                    resource.put("method", api.getHttpMethod().toUpperCase());
                    resource.put("url", "{{ base_url }}" + api.getEndpoint());
                    resource.put("description", api.getDescription());
                    if (api.getRequest() != null) {
                        Map<String, Object> body = new LinkedHashMap<>();
                        body.put("mimeType", api.getRequest().getMediaType());
                        body.put("text", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(api.getRequest().getExample()));
                        resource.put("body", body);
                    } else {
                        resource.put("body", Map.of("mimeType", "application/json", "text", ""));
                    }
                    resources.add(resource);
                }
            }

            document.put("resources", resources);
            return serialize(document);
        } catch (Exception e) {
            throw new RuntimeException("Failed creating Insomnia collection", e);
        }
    }

    private String serialize(Map<String, Object> document) throws Exception {
        return objectMapper.writeValueAsString(document)
                .replace("\"_type\":\"request\"", "\"_type\": \"request\"");
    }
}
