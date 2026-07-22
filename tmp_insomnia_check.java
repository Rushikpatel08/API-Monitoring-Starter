import com.example.api_monitoring_starter.Service.ApiRegistryService;
import com.example.api_monitoring_starter.dto.*;
import com.example.api_monitoring_starter.exporter.InsomniaExportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;

public class tmp_insomnia_check {
    public static void main(String[] args) {
        ApiRegistryService registryService = new ApiRegistryService(null) {
            @Override
            public List<ControllerDTO> getApis() { return List.of(); }
        };
        InsomniaExportService service = new InsomniaExportService(new ObjectMapper(), registryService);
        ApiEndpointDTO api = new ApiEndpointDTO(
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
        System.out.println(service.generate(api));
    }
}
