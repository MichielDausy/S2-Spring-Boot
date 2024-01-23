package fact.it.springbootapi.controller;

import fact.it.springbootapi.dto.AnomalyResponse;
import fact.it.springbootapi.dto.TrainResponse;
import fact.it.springbootapi.service.TrainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trains")
@RequiredArgsConstructor
public class TrainController {
    private final TrainService trainService;

    @GetMapping("all")
    @ResponseStatus(HttpStatus.OK)
    public List<TrainResponse> getAllTrains() {
        return trainService.getAllTrains();
    }
}
