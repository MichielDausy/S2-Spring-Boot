package fact.it.springbootapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fact.it.springbootapi.dto.AnomalyRequest;
import fact.it.springbootapi.dto.AnomalyResponse;
import fact.it.springbootapi.model.Anomaly;
import fact.it.springbootapi.service.AnomalyService;
import fact.it.springbootapi.service.FileSystemStorageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/anomalies")
@RequiredArgsConstructor
public class AnomalyController {
    private final AnomalyService anomalyService;
    private final FileSystemStorageService fileSystemStorageService;

    @PostMapping("/new")
    @ResponseStatus(HttpStatus.OK)
    public AnomalyResponse addAnomaly(@RequestParam("model") String jsonObject, @RequestParam("file") MultipartFile file) {
        AnomalyResponse response = null;
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        try {
            String fileName = fileSystemStorageService.store(file);
            response = anomalyService.addAnomaly(objectMapper.readValue(jsonObject, AnomalyRequest.class), fileName);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return response;
    }

    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public List<AnomalyResponse> getAllAnomalies(){
        return anomalyService.getAllAnomalies();
    }

    @GetMapping("/day")
    @ResponseStatus(HttpStatus.OK)
    public List<Object[]> getAllAnomaliesByDate(){
        return anomalyService.getAllAnomaliesByDate();
    }

    @GetMapping("/daySQL")
    @ResponseStatus(HttpStatus.OK)
    public List<Object[]> getAllAnomaliesByDateSQL(){
        return anomalyService.getAllAnomaliesByDateSQL();
    }
}