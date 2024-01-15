package fact.it.springbootapi.service;

import fact.it.springbootapi.dto.AnomalyRequest;
import fact.it.springbootapi.model.*;
import fact.it.springbootapi.repository.AnomalyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AnomalyService {
    private final AnomalyRepository anomalyRepository;

    public String addAnomaly(AnomalyRequest anomalyRequest) {
        try {
            Anomaly anomaly = new Anomaly();
            anomaly.setLongitude(anomalyRequest.getLongitude());
            anomaly.setLatitude(anomalyRequest.getLatitude());
            anomaly.setTimestamp(anomalyRequest.getTimestamp());

            Train train = new Train();
            train.setName(anomalyRequest.getTrain().getName());
            anomaly.setTrain(train);

            AnomalyType anomalyType = new AnomalyType();
            anomalyType.setName(anomalyRequest.getAnomaly().getName());
            anomaly.setAnomalyType(anomalyType);

            Sign sign = new Sign();
            sign.setName(anomalyRequest.getSign().getName());
            anomaly.setSign(sign);

            TrainTrack trainTrack = new TrainTrack();
            trainTrack.setName(anomalyRequest.getTrainTrack().getName());
            anomaly.setTrainTrack(trainTrack);

            Country country = new Country();
            country.setName(anomalyRequest.getCountry().getName());
            anomaly.setCountry(country);

            anomalyRepository.save(anomaly);
            return "Anomaly created";
        } catch (Exception e) {
            return "Anomaly creation failed";
        }
    }
}
