package fact.it.springbootapi.service;

import fact.it.springbootapi.dto.TrainResponse;
import fact.it.springbootapi.model.Train;
import fact.it.springbootapi.repository.TrainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TrainService {
    private final TrainRepository trainRepository;

    private TrainResponse mapToTrainResponse(Train train) {
        return TrainResponse.builder()
                .id(train.getId())
                .name(train.getName())
                .build();
    }

    public List<TrainResponse> getAllTrains() {
        List<Train> trains = trainRepository.findAll();
        return trains.stream().map(this::mapToTrainResponse).toList();
    }
}
