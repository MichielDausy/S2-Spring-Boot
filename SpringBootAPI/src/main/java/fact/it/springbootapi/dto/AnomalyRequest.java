package fact.it.springbootapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnomalyRequest {
    private Date timestamp;
    private String longitude;
    private String latitude;
    private AnomalyTypeRequest anomaly;
    private SignRequest sign;
    private TrainRequest train;
    private TrainTrackRequest trainTrack;
    private CountryRequest country;
}
