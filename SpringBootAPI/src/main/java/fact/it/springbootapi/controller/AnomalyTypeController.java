package fact.it.springbootapi.controller;

import fact.it.springbootapi.dto.AnomalyTypeResponse;
import fact.it.springbootapi.dto.TrainResponse;
import fact.it.springbootapi.service.AnomalyTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/types")
@RequiredArgsConstructor
public class AnomalyTypeController {
    private final AnomalyTypeService anomalyTypeService;

    @GetMapping("all")
    @ResponseStatus(HttpStatus.OK)
    public List<AnomalyTypeResponse> getAllAnomalyTypes() {
        return anomalyTypeService.getAllAnomalyTypes();
    }
}
