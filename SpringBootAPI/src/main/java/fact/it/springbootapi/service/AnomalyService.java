package fact.it.springbootapi.service;

import fact.it.springbootapi.dto.AnomalyRequest;
import fact.it.springbootapi.dto.AnomalyResponse;
import fact.it.springbootapi.model.*;
import fact.it.springbootapi.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326); //4326 is used for longitude and latitude coordinate systems

    private Map<LineString, Point> createRandomLineStringWithPoint(GeometryFactory geometryFactory) {
        int numPoints = 50;
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
        LineString lineString = geometryFactory.createLineString(coordinates);

        // Choose a random point on the LineString
        double randomFraction = Math.random();
        Point randomPoint = lineString.getPointN((int) (randomFraction * numPoints));

        // Create a map and store the LineString with its corresponding Point
        Map<LineString, Point> lineStringWithPointMap = new HashMap<>();
        lineStringWithPointMap.put(lineString, randomPoint);

        return lineStringWithPointMap;
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

        if (countryRepository.count() == 0) {
            Country country = new Country();
            country.setName("Belgium");
            // Create a Polygon for Belgium's area (example coordinates)
            // Example coordinates for a simplified polygon of Belgium
            Coordinate[] belgiumCoordinates = new Coordinate[]{
                    new Coordinate(2.5, 49.5),
                    new Coordinate(6.5, 49.5),
                    new Coordinate(6.5, 51.3),
                    new Coordinate(2.5, 51.3),
                    new Coordinate(2.5, 49.5)
            };

            LinearRing linearRing = geometryFactory.createLinearRing(belgiumCoordinates);
            Polygon belgiumArea = geometryFactory.createPolygon(linearRing, null);
            country.setCountryArea(belgiumArea);

            countryRepository.save(country);
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

        if (anomalyRepository.count() == 0 && trainTrackRepository.count() == 0) {
            for (int i = 0; i < count; i++) {
                Anomaly anomaly = new Anomaly();
                OffsetDateTime currentDateTime = OffsetDateTime.now();
                OffsetDateTime newDateTime = currentDateTime.plusMinutes(i);
                anomaly.setTimestamp(newDateTime); // Generates usernames User_0, User_1, ..., User_14
                anomaly.setTrain(trainRepository.findByName("Train_" + i));
                if (i % 2 != 0) {
                    anomaly.setAnomalyType(anomalyTypeRepository.findByName("Vegetation"));
                } else {
                    anomaly.setAnomalyType(anomalyTypeRepository.findByName("Sign"));
                    anomaly.setSign(signRepository.findByName("Overpass"));
                }

                TrainTrack trainTrack = new TrainTrack();
                trainTrack.setName("Track_" + i);

                Map<LineString, Point> lineStringWithPointMap = createRandomLineStringWithPoint(geometryFactory);

                // Iterate over the map entries
                for (Map.Entry<LineString, Point> entry : lineStringWithPointMap.entrySet()) {
                    LineString lineString = entry.getKey();
                    Point randomPoint = entry.getValue();

                    anomaly.setAnomalyLocation(randomPoint);
                    trainTrack.setTrackGeometry(lineString);

                    trainTrackRepository.save(trainTrack);
                    anomaly.setCountry(countryRepository.findByGeometryContains(randomPoint));
                    anomaly.setTrainTrack(trainTrackRepository.findByTrackGeometryIntersects(randomPoint));
                    anomalyRepository.save(anomaly);
                }
            }
        }
    }

    public AnomalyResponse addAnomaly(AnomalyRequest anomalyRequest, String fileName) {
        try {
            Anomaly anomaly = new Anomaly();
            anomaly.setTimestamp(anomalyRequest.getTimestamp());
            anomaly.setPhoto(fileName);

            // Set AnomalyLocation based on longitude and latitude
            Point anomalyLocation = createPoint(anomalyRequest.getLongitude(), anomalyRequest.getLatitude());
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

            //add connection to traintrack
            //Find TrainTrack based on spatial relationship
            TrainTrack trainTrack = trainTrackRepository.findByTrackGeometryIntersects(anomalyLocation);
            if (trainTrack != null) {
                anomaly.setTrainTrack(trainTrack);
            } else {
                // Handle the case when the anomalyLocation does not intersect with any TrainTrack
                anomaly.setTrainTrack(null);
            }
            //add connection to country
            // Find Country based on spatial relationship
            Country country = new Country();
            country.setName("Belgium");
            countryRepository.save(country);
            anomaly.setCountry(country);

            anomalyRepository.save(anomaly);
            return mapToAnomalyResponse(anomaly);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Method to create a Point based on longitude and latitude
    private Point createPoint(String longitude, String latitude) {
        // Parse degrees, minutes, and seconds
        double parsedLongitude = parseDMS(longitude);
        double parsedLatitude = parseDMS(latitude);
        Coordinate coordinate = new Coordinate(parsedLongitude, parsedLatitude);
        return geometryFactory.createPoint(coordinate);
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

    private AnomalyResponse mapToAnomalyResponse(Anomaly anomaly) {
        return AnomalyResponse.builder()
                .location(anomaly.getAnomalyLocation().toString())
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