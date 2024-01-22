package fact.it.springbootapi.controller;

import fact.it.springbootapi.service.TrainTrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<List<String>> showAllTracks() {
        List<String> hexGeometries =  trainTrackService.getAllTracks();
        return new ResponseEntity<>(hexGeometries, HttpStatus.OK);
    }

}
