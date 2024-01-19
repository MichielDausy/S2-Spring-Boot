package fact.it.springbootapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnomalyResponse {
    private OffsetDateTime timestamp;
    private Point location;
    private String anomalyType;
    private String sign;
    private String train;
    private String country;
    private String photo;
}