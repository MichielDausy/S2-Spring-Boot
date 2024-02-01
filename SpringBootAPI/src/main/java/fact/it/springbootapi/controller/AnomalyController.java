package fact.it.springbootapi.controller;

import fact.it.springbootapi.dto.AnomalyRequest;
import fact.it.springbootapi.dto.AnomalyResponse;
import fact.it.springbootapi.service.AnomalyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/anomalies")
@RequiredArgsConstructor
public class AnomalyController {
    private final AnomalyService anomalyService;

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