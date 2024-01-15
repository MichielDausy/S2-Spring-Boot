package fact.it.springbootapi.controller;

import fact.it.springbootapi.dto.AnomalyRequest;
import fact.it.springbootapi.dto.AnomalyResponse;
import fact.it.springbootapi.service.AnomalyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/anomalies")
@RequiredArgsConstructor
public class AnomalyController {
    private final AnomalyService anomalyService;

    @PostMapping("/new")
    @ResponseStatus(HttpStatus.OK)
    public String addAnomaly(@RequestBody AnomalyRequest anomalyRequest) {
        return anomalyService.addAnomaly(anomalyRequest);
    }

    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public List<AnomalyResponse> getAllAnomalies(){
        return anomalyService.getAllAnomalies();
    }
}
