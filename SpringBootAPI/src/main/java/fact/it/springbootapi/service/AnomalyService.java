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
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private Iterable<CSVRecord> loadFromCSV() throws Exception {
        Reader in = new FileReader("/lijnsecties.csv");
        String[] HEADERS = { "Geo Point", "Geo Shape", "Line section id","Railway line to which the section belongs","ID of the operational point at the beginning of the section","Abbreviation BVT of the operational point at the end of the section","ID of the operational point at the end of the section","Abbreviation BVT of the operational point at the beginning of the section","M-coordinate of the beginning of the section","M-coordinate of the end of the section","Installed electrification","Maximum permitted intensity of the electric power that a riding train is allowed to draw","Maximum current intensity that the train is allowed to draw when stationary","Minimum catenary’s height","Number of tracks","c400","c70","p70","p400","Symbolic name of the operational point at the beginning of the section","Symbolic name of the operational point at the end of the section"};
        //List<List<List<Double>>> lineStrings = new ArrayList<>();

        CSVFormat csvFormat = CSVFormat.newFormat(';').builder().setHeader(HEADERS).setSkipHeaderRecord(true).build();

        return csvFormat.parse(in);
    }

    private AnomalyResponse mapToAnomalyResponse(Anomaly anomaly) {
        String pointString = anomaly.getAnomalyLocation().toString();
        // Extract coordinates from the POINT string
        Pattern pattern = Pattern.compile("POINT \\((.*?) (.*?)\\)");
        Matcher matcher = pattern.matcher(pointString);
        String longitude = null;
        String latitude = null;
        if (matcher.find()) {
            longitude = matcher.group(1);
            latitude = matcher.group(2);
        }

        // Parse longitude and latitude
        double longitudeDouble = 0;
        double latitudeDouble = 0;
        if (longitude != null && latitude != null) {
            longitudeDouble = Double.parseDouble(longitude.trim());
            latitudeDouble = Double.parseDouble(latitude.trim());
        }

        return AnomalyResponse.builder()
                .id(anomaly.getId())
                .longitude(longitudeDouble)
                .latitude(latitudeDouble)
                .timestamp(anomaly.getTimestamp())
                .photo(anomaly.getPhoto())
                .anomalyTypeId(anomaly.getAnomalyType() != null ? anomaly.getAnomalyType().getId() : null)
                .countryId(anomaly.getCountry() != null ? anomaly.getCountry().getId() : null)
                .trainId(anomaly.getTrain() != null ? anomaly.getTrain().getId() : null)
                .trainTrackId(anomaly.getTrainTrack() != null ? anomaly.getTrainTrack().getId() : null)
                .isFalse(anomaly.getIsFalse())
                .isFixed(anomaly.getIsFixed())
                .count(anomaly.getCount())
                .build();
    }

    private boolean isSame(Coordinate coord1, Coordinate coord2, double thresholdDistance, String anomalyType1, String anomalyType2) {
        double distance = coord1.distance(coord2);
        boolean typeEquals = anomalyType1.equals(anomalyType2);
        return typeEquals && distance <= thresholdDistance;
    }

    @PostConstruct
    public void loadData() throws IOException, InterruptedException {
        int count = 10000;
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
                Iterable<CSVRecord> records = loadFromCSV();
                String abbreviation = "";
                String lineSectionId = "";

                int j = 0;
                for (CSVRecord record : records) {
                    List<List<Double>> lineStringCoordinates = new ArrayList<>();
                    String geoShape = record.get("Geo Shape");
                    // Remove the leading and trailing double quotes and replace "" with "
                    geoShape = geoShape.substring(1, geoShape.length() - 1).replace("\"\"", "\"");
                    JSONObject jsonObject = new JSONObject(geoShape);
                    JSONArray coordinates = jsonObject.getJSONArray("coordinates");
                    for (int i = 0; i < coordinates.length(); i++) {
                        JSONArray coordinate = coordinates.getJSONArray(i);
                        List<Double> point = new ArrayList<>();
                        point.add(coordinate.getDouble(0));
                        point.add(coordinate.getDouble(1));
                        lineStringCoordinates.add(point);
                    }
                    abbreviation = record.get("Abbreviation BVT of the operational point at the beginning of the section");
                    lineSectionId = record.get("Line section id");

                    TrainTrack trainTrack = new TrainTrack();
                    trainTrack.setName(abbreviation + " " + lineSectionId);

                    // Convert the list of coordinates to an array of Coordinate objects
                    Coordinate[] trackCoordinates = lineStringCoordinates.stream()
                            .map(point -> new Coordinate(point.get(0), point.get(1)))
                            .toArray(Coordinate[]::new);
                    // Create the LineString
                    LineString lineString = geometryFactory.createLineString(trackCoordinates);

                    // Set the anomaly's point to a point on the LineString
                    points.add(lineString.getPointN(j % lineString.getNumPoints()));

                    trainTrack.setTrackGeometry(lineString);
                    trainTrackRepository.save(trainTrack);
                    j++;
                }
                //List<List<List<Double>>> lineStringCoordinatesList = loadLineStringCoordinatesFromCSV();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (anomalyRepository.count() == 0) {
            int j = 0;
            for (int i = 0; i < count; i++) {
                Anomaly anomaly = new Anomaly();
                OffsetDateTime currentDateTime = OffsetDateTime.now();
                OffsetDateTime newDateTime = currentDateTime.plusMinutes(i);
                anomaly.setTimestamp(newDateTime); // Generates usernames User_0, User_1, ..., User_14
                anomaly.setTrain(trainRepository.findByName("Train_" + i));
                anomaly.setCount(1);
                anomaly.setIsFalse(false);
                anomaly.setIsFixed(false);
                if (i % 2 != 0) {
                    anomaly.setAnomalyType(anomalyTypeRepository.findByName("Vegetation"));
                } else {
                    anomaly.setAnomalyType(anomalyTypeRepository.findByName("Sign"));
                }
                anomaly.setAnomalyLocation(points.get(i));
                List<TrainTrack> trainTracks = trainTrackRepository.findByTrackGeometryIntersects(points.get(i));
                anomaly.setTrainTrack(trainTracks.get(0));
                if (j == points.size()) {
                    j = 0;
                }
                anomaly.setCountry(countryRepository.findByGeometryContains(points.get(j)));
                anomalyRepository.save(anomaly);
                j++;
            }
        }
    }

    public AnomalyResponse addAnomaly(AnomalyRequest anomalyRequest, String fileName) {
        Coordinate coordinate = new Coordinate(Double.parseDouble(anomalyRequest.getLongitude()), Double.parseDouble(anomalyRequest.getLatitude()));
        Point anomalyPoint = geometryFactory.createPoint(coordinate);
        Anomaly closestAnomaly = anomalyRepository.findClosestAnomaly(anomalyPoint, anomalyRequest.getAnomalyType());

        // Define a threshold distance
        double thresholdDistanceMeters = 5.0;
        double thresholdDistance = thresholdDistanceMeters / 111000;
        //this means that if an anomaly is within 5 meters from another anomaly then the count is increased of the already existing anomaly and no new anomaly is created because it is very likely the same one.

        if (closestAnomaly != null) {
            // Check if the closest anomaly is within the threshold distance and that both anomalies have the same anomaly type
            if (isSame(coordinate, closestAnomaly.getAnomalyLocation().getCoordinate(), thresholdDistance, closestAnomaly.getAnomalyType().getName(), anomalyRequest.getAnomalyType())) {
                //increment the number of detections of that anomaly
                int count = closestAnomaly.getCount() + 1;
                //closestAnomaly.setId(closestAnomaly.getId()); //prevents the creating of a new anomaly
                closestAnomaly.setCount(count);
                anomalyRepository.save(closestAnomaly);
                return mapToAnomalyResponse(closestAnomaly);
            } else {
                // create new anomaly
                Anomaly anomaly = new Anomaly();
                anomaly.setTimestamp(anomalyRequest.getTimestamp());
                anomaly.setPhoto(fileName);
                anomaly.setCount(1);
                anomaly.setIsFixed(false);
                anomaly.setIsFalse(false);

                // Set AnomalyLocation based on coordinate
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
                TrainTrack trainTrack = trainTrackRepository.findClosestTrainTrack(anomalyLocation);
                anomaly.setTrainTrack(trainTrack);
                //add connection to country
                anomaly.setCountry(countryRepository.findByGeometryContains(anomalyLocation));

                anomalyRepository.save(anomaly);
                return mapToAnomalyResponse(anomaly);
            }
        }
        return null;
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

    public List<AnomalyResponse> getAllAnomaliesByTrainTrack(Integer id) {
        List<Anomaly> anomalies = anomalyRepository.findAllByTrainTrack_Id(id);
        return anomalies.stream().map(this::mapToAnomalyResponse).toList();
    }
}