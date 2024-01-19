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

import java.util.List;

@RestController
@RequestMapping("/api/anomalies")
@RequiredArgsConstructor
public class AnomalyController {
    private final AnomalyService anomalyService;
    private final FileSystemStorageService fileSystemStorageService;

    @PostMapping("/new")
    @ResponseStatus(HttpStatus.OK)
    public AnomalyResponse addAnomaly(@ModelAttribute AnomalyRequest data, @RequestParam("file") MultipartFile file) {
        AnomalyResponse response = null;
        try {
            String fileName = fileSystemStorageService.store(file, data.getTimestamp().toString());
            response = anomalyService.addAnomaly(data, fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    @GetMapping("all")
    @ResponseStatus(HttpStatus.OK)
    public List<AnomalyResponse> getAnomalies() {
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