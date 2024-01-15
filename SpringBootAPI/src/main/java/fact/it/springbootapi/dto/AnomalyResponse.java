package fact.it.springbootapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnomalyResponse {
    private Date timestamp;
    private String longitude;
    private String latitude;
    private String anomalyType;
    private String sign;
    private String train;
    private String trainTrack;
    private String country;
}
