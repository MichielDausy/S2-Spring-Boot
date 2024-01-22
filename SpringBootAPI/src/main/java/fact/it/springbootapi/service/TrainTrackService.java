package fact.it.springbootapi.service;

import fact.it.springbootapi.repository.TrainTrackRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class TrainTrackService {
    private final TrainTrackRepository trainTrackRepository;
    public String findTrackById(Long trackId) {
        return trainTrackRepository.findTrackGeometryAsText(trackId);
    }

    public List<String> getAllTracks() {
        return trainTrackRepository.getAllTraintracks();
    }
}
