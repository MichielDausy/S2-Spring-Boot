package com.example.s2angularapi.service;

import com.example.s2angularapi.dto.AnomalyRequest;
import com.example.s2angularapi.dto.AnomalyResponse;
import com.example.s2angularapi.model.Anomaly;
import com.example.s2angularapi.model.AnomalyType;
import com.example.s2angularapi.model.Sign;
import com.example.s2angularapi.model.Train;
import com.example.s2angularapi.repository.AnomalyRepository;
import com.example.s2angularapi.repository.AnomalyTypeRepository;
import com.example.s2angularapi.repository.SignRepository;
import com.example.s2angularapi.repository.TrainRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.*;
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
    private final SignRepository signRepository;
    private final TrainRepository trainRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326); //4326 is used for longitude and latitude coordinate systems

    private AnomalyResponse mapToAnomalyResponse(Anomaly anomaly) {
        AnomalyResponse anomalyResponse = new AnomalyResponse();
        anomalyResponse.setLocation(anomaly.getAnomalyLocation());
        anomalyResponse.setTimestamp(anomaly.getTimestamp());
        anomalyResponse.setPhoto(anomaly.getPhoto());
        anomalyResponse.setAnomalyType(anomaly.getAnomalyType() != null ? anomaly.getAnomalyType().getName() : null);
        anomalyResponse.setSign(anomaly.getSign() != null ? anomaly.getSign().getName() : null);
        anomalyResponse.setTrain(anomaly.getTrain() != null ? anomaly.getTrain().getName() : null);
        return anomalyResponse;
        /*return AnomalyResponse.builder()
                .location(anomaly.getAnomalyLocation())
                .timestamp(anomaly.getTimestamp())
                .photo(anomaly.getPhoto())
                .anomalyType(anomaly.getAnomalyType() != null ? anomaly.getAnomalyType().getName() : null)
                .sign(anomaly.getSign() != null ? anomaly.getSign().getName() : null)
                .train(anomaly.getTrain() != null ? anomaly.getTrain().getName() : null)
                .build();*/
    }

    @PostConstruct
    public void loadData() {
        int count = 15;
        if (trainRepository.count() == 0) {
            for (int i = 0; i < count; i++) {
                Train train = new Train();
                train.setName("Train_" + i);

                trainRepository.save(train);
            }
        }

        if (anomalyTypeRepository.count() == 0) {
            AnomalyType vegetation = new AnomalyType();
            vegetation.setName("Vegetation");

            AnomalyType sign = new AnomalyType();
            sign.setName("Sign");

            anomalyTypeRepository.save(vegetation);
            anomalyTypeRepository.save(sign);
        }

        if (signRepository.count() == 0) {
            Sign stop = new Sign();
            stop.setName("Stop");

            Sign overpass = new Sign();
            overpass.setName("Overpass");

            signRepository.save(stop);
            signRepository.save(overpass);
        }

        if (anomalyRepository.count() == 0) {
            for (int i = 0; i < count; i++) {
                Anomaly anomaly = new Anomaly();
                OffsetDateTime currentDateTime = OffsetDateTime.now();
                OffsetDateTime newDateTime = currentDateTime.plusMinutes(i);
                anomaly.setTimestamp(newDateTime); // Generates usernames User_0, User_1, ..., User_14
                Train train = trainRepository.findByName("Train_" + i);
                anomaly.setTrain(train);
                if (i % 2 != 0) {
                    AnomalyType anomalyType = anomalyTypeRepository.findByName("Vegetation");
                    anomaly.setAnomalyType(anomalyType);
                } else {
                    AnomalyType anomalyType = anomalyTypeRepository.findByName("Sign");
                    anomaly.setAnomalyType(anomalyType);
                    Sign sign = signRepository.findByName("Overpass");
                    anomaly.setSign(sign);
                }

                //a random point within Belgium bounds
                double startX = 2.5 + Math.random() * (6.5 - 2.5);
                double startY = 49.5 + Math.random() * (51.3 - 49.5);
                Coordinate coordinate = new Coordinate(startX, startY);
                Point point = geometryFactory.createPoint(coordinate);
                anomaly.setAnomalyLocation(point);

                anomalyRepository.save(anomaly);
            }
        }
    }

    public List<AnomalyResponse> getAllAnomalies() {
        List<Anomaly> anomalies = anomalyRepository.findAll();
        List<AnomalyResponse> anomalyResponses = anomalies.stream().map(this::mapToAnomalyResponse).toList();
        return anomalyResponses;
    }

    public AnomalyResponse addAnomaly(AnomalyRequest anomalyRequest, String fileName) {
        try {
            Anomaly anomaly = new Anomaly();
            anomaly.setTimestamp(anomalyRequest.getTimestamp());
            anomaly.setPhoto(fileName);

            // Set AnomalyLocation based on longitude and latitude
            double parsedLongitude = parseDMS(anomalyRequest.getLongitude());
            double parsedLatitude = parseDMS(anomalyRequest.getLatitude());
            Coordinate coordinate = new Coordinate(parsedLongitude, parsedLatitude);
            Point anomalyLocation = geometryFactory.createPoint(coordinate);
            anomaly.setAnomalyLocation(anomalyLocation);

            //add connection to train
            Train train = trainRepository.findByName(anomalyRequest.getTrain());
            anomaly.setTrain(train);
            //add connection to sign
            Sign sign = signRepository.findByName(anomalyRequest.getSign());
            anomaly.setSign(sign);
            //add connection to anomaly type
            AnomalyType anomalyType = anomalyTypeRepository.findByName(anomalyRequest.getAnomalyType());
            anomaly.setAnomalyType(anomalyType);

            anomalyRepository.save(anomaly);
            return mapToAnomalyResponse(anomaly);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Method to parse degrees, minutes, and seconds
    private double parseDMS(String dms) {
        String[] parts = dms.split("[°'']");

        double degrees = Double.parseDouble(parts[0]);
        double minutes = Double.parseDouble(parts[1]);
        double seconds = Double.parseDouble(parts[2]);

        double decimalDegrees = degrees + minutes / 60.0 + seconds / 3600.0;

        // Check if it's west (negative longitude) or south (negative latitude)
        if (dms.endsWith("W") || dms.endsWith("S")) {
            decimalDegrees = -decimalDegrees;
        }
        return decimalDegrees;
    }
}
