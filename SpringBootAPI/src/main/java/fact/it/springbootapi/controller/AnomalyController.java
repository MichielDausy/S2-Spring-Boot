package fact.it.springbootapi.controller;

import fact.it.springbootapi.dto.AnomalyRequest;
import fact.it.springbootapi.dto.AnomalyResponse;
import fact.it.springbootapi.service.AnomalyService;
import fact.it.springbootapi.service.FileSystemStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
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

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public AnomalyResponse getAnomalyById(@RequestParam Integer id) {
        return anomalyService.getAnomalyById(id);
    }

    @GetMapping("/map")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<String>> getAllAnomaliesOnMap() {
        List<String> points =  anomalyService.getAllAnomaliesOnMap();
        return new ResponseEntity<>(points, HttpStatus.OK);
    }

    @PutMapping("/mark")
    @ResponseStatus(HttpStatus.OK)
    public AnomalyResponse markAnomaly(@RequestBody AnomalyRequest anomalyRequest){
        return anomalyService.markAnomaly(anomalyRequest);
    }

    @GetMapping("/byTrack")
    @ResponseStatus(HttpStatus.OK)
    public List<AnomalyResponse> getAnomaliesByTrack(@RequestParam Integer id) {
        return anomalyService.getAllAnomaliesByTrainTrack(id);
    }
}