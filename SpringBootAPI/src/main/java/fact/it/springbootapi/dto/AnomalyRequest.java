package fact.it.springbootapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnomalyRequest {
    private OffsetDateTime timestamp;
    private String longitude;
    private String latitude;
    private String anomalyType;
    private String sign;
    private String train;
}