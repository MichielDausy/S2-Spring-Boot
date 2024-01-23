package fact.it.springbootapi.controller;

import fact.it.springbootapi.dto.TrainTrackResponse;
import fact.it.springbootapi.service.TrainTrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/tracks")
@RequiredArgsConstructor
public class TrainTrackController {
    private final TrainTrackService trainTrackService;

    @GetMapping("/map")
    public ResponseEntity<String> showMap(@RequestParam Long trackId) {
        // Retrieve hex geometry directly from the service
        String hexGeometry = trainTrackService.findTrackById(trackId);
        return new ResponseEntity<>(hexGeometry, HttpStatus.OK);
    }

    @GetMapping("/all")
    public List<TrainTrackResponse> getAllTracks() {
        return trainTrackService.getAllTracks();
    }

}
