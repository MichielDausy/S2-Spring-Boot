package fact.it.springbootapi.service;

import fact.it.springbootapi.dto.AnomalyTypeResponse;
import fact.it.springbootapi.model.AnomalyType;
import fact.it.springbootapi.repository.AnomalyTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AnomalyTypeService {
    private final AnomalyTypeRepository anomalyTypeRepository;

    private AnomalyTypeResponse mapToAnomalyTypeResponse(AnomalyType anomalyType) {
        return AnomalyTypeResponse.builder()
                .id(anomalyType.getId())
                .name(anomalyType.getName())
                .build();
    }

    public List<AnomalyTypeResponse> getAllAnomalyTypes() {
        List<AnomalyType> anomalyTypeList = anomalyTypeRepository.findAll();
        return anomalyTypeList.stream().map(this::mapToAnomalyTypeResponse).toList();
    }
}
