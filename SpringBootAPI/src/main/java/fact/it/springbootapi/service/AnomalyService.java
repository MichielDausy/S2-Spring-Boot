package fact.it.springbootapi.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;

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
    private final String filePath = "/coordinates.txt";

    private AmazonS3 s3client;
    @Value("${amazonProperties.bucketName}")
    private String bucketName;
    @Value("${amazonProperties.accessKey}")
    private String accessKey;
    @Value("${amazonProperties.secretKey}")
    private String secretKey;
    @Value("${amazonProperties.sessiontoken}")
    private String sessiontoken;

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
                .anomalyTypeId(anomaly.getAnomalyType() != null ? anomaly.getAnomalyType().getId() : null)
                .countryId(anomaly.getCountry() != null ? anomaly.getCountry().getId() : null)
                .trainId(anomaly.getTrain() != null ? anomaly.getTrain().getId() : null)
                .trainTrackId(anomaly.getTrainTrack() != null ? anomaly.getTrainTrack().getId() : null)
                .isFalse(anomaly.getIsFalse())
                .isFixed(anomaly.getIsFixed())
                .count(anomaly.getCount())
                .build();
    }

    private AnomalyResponse mapToAnomalyResponse(Anomaly anomaly, S3ObjectInputStream s3ObjectInputStream) {
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
                .photo(s3ObjectInputStream)
                .anomalyTypeId(anomaly.getAnomalyType() != null ? anomaly.getAnomalyType().getId() : null)
                .countryId(anomaly.getCountry() != null ? anomaly.getCountry().getId() : null)
                .trainId(anomaly.getTrain() != null ? anomaly.getTrain().getId() : null)
                .trainTrackId(anomaly.getTrainTrack() != null ? anomaly.getTrainTrack().getId() : null)
                .isFalse(anomaly.getIsFalse())
                .isFixed(anomaly.getIsFixed())
                .count(anomaly.getCount())
                .build();
    }

    @PostConstruct
    private void initializeAmazon() {
        AWSCredentials credentials = new BasicSessionCredentials(this.accessKey, this.secretKey, this.sessiontoken);
        this.s3client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
    }

    public void loadData() throws IOException, InterruptedException {
        int count = 1000;
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
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                // Create a list to store polygons
                List<Polygon> polygons = new ArrayList<>();

                // Iterate through each line in the file
                while ((line = reader.readLine()) != null) {
                    // Parse coordinates from the line
                    // Remove brackets and split the line by comma
                    String[] pairs = line.replaceAll("[\\[\\](){}]", "").split(", ");

                    List<Coordinate> coordinates = new ArrayList<>();

                    // Parse each pair and add to the list
                    for (int i = 0; i < pairs.length; i += 2) {
                        // Ensure that there are enough elements in the pairs array
                        if (i + 1 < pairs.length) {
                            // Extract x and y values from pairs[i] and pairs[i+1]
                            double x = Double.parseDouble(pairs[i]);
                            double y = Double.parseDouble(pairs[i + 1]);
                            coordinates.add(new Coordinate(x, y));
                        } else {
                            // Handle the case where there are not enough elements in the pairs array
                            System.err.println("Invalid pair format: " + line);
                        }
                    }

                    // Create a polygon using the GeometryFactory
                    Coordinate[] coordsArray = coordinates.toArray(new Coordinate[0]);
                    Polygon polygon = geometryFactory.createPolygon(coordsArray);

                    polygons.add(polygon);
                }
                // Set the list of polygons to the country
                Polygon[] polygonArray = polygons.toArray(new Polygon[0]);
                country.setCountryArea(geometryFactory.createMultiPolygon(polygonArray));

                // Save the country with its associated polygons
                countryRepository.save(country);
            } catch (IOException e) {
                e.printStackTrace();
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
            Random random = new Random();
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
                if (j == points.size()-1) {
                    j = 0;
                }
                // Get a random point on the train track's LineString
                Optional<TrainTrack> trainTrack = trainTrackRepository.findById(j);
                if (trainTrack.isPresent()) {
                    LineString lineString = trainTrack.get().getTrackGeometry();
                    int randomIndex = random.nextInt(lineString.getNumPoints());
                    Point randomPoint = lineString.getPointN(randomIndex);
                    anomaly.setAnomalyLocation(randomPoint);

                    List<TrainTrack> trainTracks = trainTrackRepository.findByTrackGeometryIntersects(randomPoint);
                    anomaly.setTrainTrack(trainTracks.get(0));
                    anomaly.setCountry(countryRepository.findByGeometryContains(randomPoint));
                    anomalyRepository.save(anomaly);
                }
                j++;
            }
        }
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
        String fileName = anomaly.getPhoto();
        S3Object s3Object = s3client.getObject(bucketName, fileName);
        S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();
        return mapToAnomalyResponse(anomaly, s3ObjectInputStream);
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