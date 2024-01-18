package fact.it.springbootapi.service;

import fact.it.springbootapi.dto.AnomalyRequest;
import fact.it.springbootapi.dto.AnomalyResponse;
import fact.it.springbootapi.model.*;
import fact.it.springbootapi.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AnomalyService {
    private final AnomalyRepository anomalyRepository;
    private final AnomalyTypeRepository anomalyTypeRepository;
    private final CountryRepository countryRepository;
    private final SignRepository signRepository;
    private final TrainRepository trainRepository;
    private final TrainTrackRepository trainTrackRepository;

    private LineString createRandomLineString(GeometryFactory geometryFactory) {
        int numPoints = 10;
        Coordinate[] coordinates = new Coordinate[numPoints];

        // Start with a random point within Belgium bounds
        double startX = 2.5 + Math.random() * (6.5 - 2.5);
        double startY = 49.5 + Math.random() * (51.3 - 49.5);
        coordinates[0] = new Coordinate(startX, startY);

        // Generate subsequent points within a reasonable range from the previous point
        for (int i = 1; i < numPoints; i++) {
            double deltaX = -0.1 + Math.random() * 0.2; // Small change in longitude
            double deltaY = -0.05 + Math.random() * 0.1; // Small change in latitude

            double newX = coordinates[i - 1].getX() + deltaX;
            double newY = coordinates[i - 1].getY() + deltaY;

            // Ensure the new coordinates are within Belgium bounds
            newX = Math.max(2.5, Math.min(6.5, newX));
            newY = Math.max(49.5, Math.min(51.3, newY));

            coordinates[i] = new Coordinate(newX, newY);
        }

        return geometryFactory.createLineString(coordinates);
    }

    @PostConstruct
    public void loadData() {
        if (anomalyRepository.count() == 0) {
            for (int i = 0; i < 15; i++) {
                Anomaly anomaly = new Anomaly();
                OffsetDateTime currentDateTime = OffsetDateTime.now();
                OffsetDateTime newDateTime = currentDateTime.plusMinutes(i);
                anomaly.setTimestamp(newDateTime); // Generates usernames User_0, User_1, ..., User_14
                anomaly.setLongitude(i + "°" + i + "'" + i + "''E");
                anomaly.setLatitude(i + "°" + i + "'" + i + "''N");

                anomalyRepository.save(anomaly);
            }
        }
        if (trainTrackRepository.count() == 0) {
            GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326); //4326 is used for longitude and latitude coordinate systems

            for (int i = 0; i < 15; i++) {
                TrainTrack trainTrack = new TrainTrack();
                trainTrack.setName("Track_" + i);

                // Create a LineString with random coordinates
                LineString lineString = createRandomLineString(geometryFactory);
                trainTrack.setTrackGeometry(lineString);

                trainTrackRepository.save(trainTrack);
            }
        }
    }

    public AnomalyResponse addAnomaly(AnomalyRequest anomalyRequest, String fileName) {
        try {
            Anomaly anomaly = new Anomaly();
            anomaly.setLongitude(anomalyRequest.getLongitude());
            anomaly.setLatitude(anomalyRequest.getLatitude());
            anomaly.setTimestamp(anomalyRequest.getTimestamp());
            anomaly.setPhoto(fileName);

            Train train = new Train();
            train.setName(anomalyRequest.getTrain().getName());
            trainRepository.save(train);
            anomaly.setTrain(train);

            AnomalyType anomalyType = new AnomalyType();
            anomalyType.setName(anomalyRequest.getAnomalyType().getName());
            anomalyTypeRepository.save(anomalyType);
            anomaly.setAnomalyType(anomalyType);

            Sign sign = new Sign();
            sign.setName(anomalyRequest.getSign().getName());
            signRepository.save(sign);
            anomaly.setSign(sign);

            TrainTrack trainTrack = new TrainTrack();
            trainTrack.setName(anomalyRequest.getTrainTrack().getName());
            trainTrackRepository.save(trainTrack);
            anomaly.setTrainTrack(trainTrack);

            Country country = new Country();
            country.setName(anomalyRequest.getCountry().getName());
            countryRepository.save(country);
            anomaly.setCountry(country);

            anomalyRepository.save(anomaly);
            return mapToAnomalyResponse(anomaly);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private AnomalyResponse mapToAnomalyResponse(Anomaly anomaly) {
        return AnomalyResponse.builder()
                .latitude(anomaly.getLatitude())
                .longitude(anomaly.getLongitude())
                .timestamp(anomaly.getTimestamp())
                .photo(anomaly.getPhoto())
                .anomalyType(anomaly.getAnomalyType() != null ? anomaly.getAnomalyType().getName() : null)
                .country(anomaly.getCountry() != null ? anomaly.getCountry().getName() : null)
                .sign(anomaly.getSign() != null ? anomaly.getSign().getName() : null)
                .train(anomaly.getTrain() != null ? anomaly.getTrain().getName() : null)
                .trainTrack(anomaly.getTrainTrack() != null ? anomaly.getTrainTrack().getName() : null)
                .build();
    }

    public List<AnomalyResponse> getAllAnomalies() {
        List<Anomaly> anomalies = anomalyRepository.findAll();
        return anomalies.stream().map(this::mapToAnomalyResponse).toList();
    }

    public List<Object[]> getAllAnomaliesByDate() {
        return anomalyRepository.getAnomaliesByDay();
    }

    public List<Object[]> getAllAnomaliesByDateSQL() {
        return anomalyRepository.getAnomaliesByDaySQL();
    }
}