package fact.it.springbootapi.service;

import fact.it.springbootapi.dto.AnomalyRequest;
import fact.it.springbootapi.dto.AnomalyResponse;
import fact.it.springbootapi.model.*;
import fact.it.springbootapi.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
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

    @PostConstruct
    public void loadData() {
        if (anomalyRepository.count() == 0) {
            for (int i = 0; i < 15; i++) {
                Anomaly anomaly = new Anomaly();
                anomaly.setTimestamp(new Date()); // Generates usernames User_0, User_1, ..., User_14
                anomaly.setLongitude(i + "°" + i + "'" + i + "''E");
                anomaly.setLatitude(i + "°" + i + "'" + i + "''N");

                anomalyRepository.save(anomaly);
            }
        }
    }

    public String addAnomaly(AnomalyRequest anomalyRequest) {
        try {
            Anomaly anomaly = new Anomaly();
            anomaly.setLongitude(anomalyRequest.getLongitude());
            anomaly.setLatitude(anomalyRequest.getLatitude());
            anomaly.setTimestamp(anomalyRequest.getTimestamp());

            Train train = new Train();
            train.setName(anomalyRequest.getTrain().getName());
            trainRepository.save(train);
            anomaly.setTrain(train);

            AnomalyType anomalyType = new AnomalyType();
            anomalyType.setName(anomalyRequest.getAnomaly().getName());
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
            return "Anomaly created";
        } catch (Exception e) {
            return e.toString();
        }
    }

    private AnomalyResponse mapToAnomalyResponse(Anomaly anomaly) {
        return AnomalyResponse.builder()
                .latitude(anomaly.getLatitude())
                .longitude(anomaly.getLongitude())
                .timestamp(anomaly.getTimestamp())
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
}