package fact.it.springbootapi.service;

import fact.it.springbootapi.dto.CoordinateResponse;
import fact.it.springbootapi.dto.TrainTrackResponse;
import fact.it.springbootapi.model.TrainTrack;
import fact.it.springbootapi.repository.TrainTrackRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKTReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional
public class TrainTrackService {
    private final TrainTrackRepository trainTrackRepository;

    private TrainTrackResponse mapToTrainTrackResponse(TrainTrack trainTrack) {
        LineString lineString = trainTrack.getTrackGeometry();
        Coordinate[] coords = lineString.getCoordinates();

        List<CoordinateResponse> coordinates = Arrays.stream(coords).map(coord -> new CoordinateResponse(coord.getX(), coord.getY())).toList();

        return TrainTrackResponse.builder()
                .id(trainTrack.getId())
                .name(trainTrack.getName())
                .trackGeometry(coordinates)
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
