package fact.it.springbootapi.service;

import fact.it.springbootapi.dto.TrainTrackResponse;
import fact.it.springbootapi.model.TrainTrack;
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

    private TrainTrackResponse mapToTrainTrackResponse(TrainTrack trainTrack) {
        return TrainTrackResponse.builder()
                .id(trainTrack.getId())
                .name(trainTrack.getName())
                .trackGeometry(trainTrack.getTrackGeometry().toString())
                .build();
    }

    public String findTrackById(Long trackId) {
        return trainTrackRepository.findTrackGeometryAsText(trackId);
    }

    public List<TrainTrackResponse> getAllTracks() {
        List<TrainTrack> trainTracks = trainTrackRepository.findAll();
        return trainTracks.stream().map(this::mapToTrainTrackResponse).toList();
    }
}
