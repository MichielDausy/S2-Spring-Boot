package fact.it.springbootapi.service;

import fact.it.springbootapi.dto.AnomalyRequest;
import fact.it.springbootapi.dto.AnomalyResponse;
import fact.it.springbootapi.model.*;
import fact.it.springbootapi.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.locationtech.jts.geom.*;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AnomalyService {
    private final AnomalyRepository anomalyRepository;
    private final AnomalyTypeRepository anomalyTypeRepository;
    private final CountryRepository countryRepository;
    private final TrainRepository trainRepository;
    private final TrainTrackRepository trainTrackRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326); //4326 is used for longitude and latitude coordinate systems

    private List<List<List<Double>>> loadLineStringCoordinatesFromCSV() throws IOException, JSONException {
        Reader in = new FileReader("lijnsecties.csv");
        String[] HEADERS = { "Geo Point", "Geo Shape", "Line section id","Railway line to which the section belongs","ID of the operational point at the beginning of the section","Abbreviation BVT of the operational point at the end of the section","ID of the operational point at the end of the section","Abbreviation BVT of the operational point at the beginning of the section","M-coordinate of the beginning of the section","M-coordinate of the end of the section","Installed electrification","Maximum permitted intensity of the electric power that a riding train is allowed to draw","Maximum current intensity that the train is allowed to draw when stationary","Minimum catenary’s height","Number of tracks","c400","c70","p70","p400","Symbolic name of the operational point at the beginning of the section","Symbolic name of the operational point at the end of the section"};
        List<List<List<Double>>> lineStrings = new ArrayList<>();

        CSVFormat csvFormat = CSVFormat.newFormat(';').builder().setHeader(HEADERS).setSkipHeaderRecord(true).build();

        Iterable<CSVRecord> records = csvFormat.parse(in);

        for (CSVRecord record : records) {
            String geoShape = record.get("Geo Shape");
            // Remove the leading and trailing double quotes and replace "" with "
            geoShape = geoShape.substring(1, geoShape.length() - 1).replace("\"\"", "\"");
            JSONObject jsonObject = new JSONObject(geoShape);
            JSONArray coordinates = jsonObject.getJSONArray("coordinates");
            List<List<Double>> lineStringCoordinates = new ArrayList<>();
            for (int i = 0; i < coordinates.length(); i++) {
                JSONArray coordinate = coordinates.getJSONArray(i);
                List<Double> point = new ArrayList<>();
                point.add(coordinate.getDouble(0));
                point.add(coordinate.getDouble(1));
                lineStringCoordinates.add(point);
            }
            lineStrings.add(lineStringCoordinates);
        }
        return lineStrings;
    }

    @PostConstruct
    public void loadData() throws IOException, InterruptedException {
        int count = 100;
        List<Point> points = new ArrayList<>();
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

        if (trainTrackRepository.count() == 0) {
            try {
                // Load LineString coordinates from CSV
                List<List<List<Double>>> lineStringCoordinatesList = loadLineStringCoordinatesFromCSV();

                int j = 0;
                for (List<List<Double>> lineStringCoordinates : lineStringCoordinatesList) {
                    TrainTrack trainTrack = new TrainTrack();
                    trainTrack.setName("Train_" + Math.random());

                    // Convert the list of coordinates to an array of Coordinate objects
                    Coordinate[] coordinates = lineStringCoordinates.stream()
                            .map(point -> new Coordinate(point.get(0), point.get(1)))
                            .toArray(Coordinate[]::new);
                    // Create the LineString
                    LineString lineString = geometryFactory.createLineString(coordinates);

                    // Set the anomaly's point to a point on the LineString
                    points.add(lineString.getPointN(j % lineString.getNumPoints()));

                    trainTrack.setTrackGeometry(lineString);
                    trainTrackRepository.save(trainTrack);
                    j++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        if (anomalyRepository.count() == 0) {
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
                }
                anomaly.setAnomalyLocation(points.get(i));
                List<TrainTrack> trainTracks = trainTrackRepository.findByTrackGeometryIntersects(points.get(i));
                anomaly.setTrainTrack(trainTracks.get(0));
                anomaly.setCountry(countryRepository.findByGeometryContains(points.get(i)));
                anomalyRepository.save(anomaly);
            }
        }
    }

    public AnomalyResponse addAnomaly(AnomalyRequest anomalyRequest, String fileName) {
        try {
            Anomaly anomaly = new Anomaly();
            anomaly.setTimestamp(anomalyRequest.getTimestamp());
            anomaly.setPhoto(fileName);

            // Set AnomalyLocation based on longitude and latitude
            Coordinate coordinate = new Coordinate(Double.parseDouble(anomalyRequest.getLongitude()), Double.parseDouble(anomalyRequest.getLatitude()));
            Point anomalyLocation = geometryFactory.createPoint(coordinate);
            anomaly.setAnomalyLocation(anomalyLocation);

            //add connection to train
            Train train = trainRepository.findByName(anomalyRequest.getTrain());
            anomaly.setTrain(train);
            //add connection to anomaly type
            AnomalyType anomalyType = anomalyTypeRepository.findByName(anomalyRequest.getAnomalyType());
            anomaly.setAnomalyType(anomalyType);

            //add connection to traintrack
            //Find TrainTrack based on spatial relationship
            List<TrainTrack> trainTracks = trainTrackRepository.findClosestTrainTrack(anomalyLocation);
            if (trainTracks != null) {
                anomaly.setTrainTrack(trainTracks.get(0));
            } else {
                // Handle the case when the anomalyLocation does not intersect with any TrainTrack
                anomaly.setTrainTrack(null);
            }
            //add connection to country
            anomaly.setCountry(countryRepository.findByGeometryContains(anomalyLocation));

            anomalyRepository.save(anomaly);
            return mapToAnomalyResponse(anomaly);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private AnomalyResponse mapToAnomalyResponse(Anomaly anomaly) {
        return AnomalyResponse.builder()
                .id(anomaly.getId())
                .location(anomaly.getAnomalyLocation().toString())
                .timestamp(anomaly.getTimestamp())
                .photo(anomaly.getPhoto())
                .anomalyType(anomaly.getAnomalyType() != null ? anomaly.getAnomalyType().getName() : null)
                .country(anomaly.getCountry() != null ? anomaly.getCountry().getName() : null)
                .train(anomaly.getTrain() != null ? anomaly.getTrain().getName() : null)
                .trainTrack(anomaly.getTrainTrack() != null ? anomaly.getTrainTrack().getName() : null)
                .isFalse(anomaly.getIsFalse())
                .isFixed(anomaly.getIsFixed())
                .build();
    }

    public List<AnomalyResponse> getAllAnomalies() {
        List<Anomaly> anomalies = anomalyRepository.findAll();
        return anomalies.stream().map(this::mapToAnomalyResponse).toList();
    }

    public List<String> getAllAnomaliesOnMap() {
        return anomalyRepository.getAllAnomaliesOnMap();
    }

    public AnomalyResponse getAnomalyById(Integer id) {
        Anomaly anomaly = anomalyRepository.findByIdEquals(id);
        return mapToAnomalyResponse(anomaly);
    }

    public AnomalyResponse markAnomaly(AnomalyRequest anomalyRequest) {
        Anomaly anomaly = anomalyRepository.findByIdEquals(anomalyRequest.getId());
        if (anomalyRequest.getIsFixed() != null) {
            anomaly.setIsFixed(anomalyRequest.getIsFixed());
        }
        if (anomalyRequest.getIsFalse() != null) {
            anomaly.setIsFalse(anomalyRequest.getIsFalse());
        }
        anomalyRepository.save(anomaly);
        return mapToAnomalyResponse(anomaly);
    }
}