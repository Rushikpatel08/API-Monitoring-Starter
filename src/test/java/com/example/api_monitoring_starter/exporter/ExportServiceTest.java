package com.example.api_monitoring_starter.exporter;

import com.example.api_monitoring_starter.Service.ApiRegistryService;
import com.example.api_monitoring_starter.dto.ApiEndpointDTO;
import com.example.api_monitoring_starter.dto.ApiParameterDTO;
import com.example.api_monitoring_starter.dto.ApiRequestDTO;
import com.example.api_monitoring_starter.dto.ApiResponseDTO;
import com.example.api_monitoring_starter.dto.ControllerDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportServiceTest {

    @Test
    void insomniaSingleExportShouldContainRequestPayload() throws Exception {
        ApiRegistryService registryService = Mockito.mock(ApiRegistryService.class);
        InsomniaExportService service = new InsomniaExportService(new ObjectMapper(), registryService);

        ApiEndpointDTO api = buildApi();
        String exported = service.generate(api);

        assertTrue(exported.contains("\"_type\": \"request\""));
        assertTrue(exported.contains("/students"));
    }

    @Test
    void postmanCollectionExportShouldContainAllEndpoints() throws Exception {
        ApiRegistryService registryService = Mockito.mock(ApiRegistryService.class);
        Mockito.when(registryService.getApis()).thenReturn(List.of(
                new ControllerDTO("Students", "students", "Student endpoints", List.of(buildApi()))
        ));

        PostmanExportService service = new PostmanExportService(new ObjectMapper(), registryService);
        String exported = service.generateCollection("all");

        assertTrue(exported.contains("\"info\""));
        assertTrue(exported.contains("\"item\""));
        assertTrue(exported.contains("/students"));
    }

    private ApiEndpointDTO buildApi() {
        return new ApiEndpointDTO(
                "student-get",
                "GET",
                "/students",
                "getStudents",
                List.of(new ApiParameterDTO("page", "query", "int", true)),
                new ApiResponseDTO(200, "OK", "application/json", Map.of("id", 1), null, null),
                new ApiRequestDTO("application/json", Map.of("name", "John"), Map.of()),
                null,
                "Get students",
                "Returns students"
        );
    }
}
