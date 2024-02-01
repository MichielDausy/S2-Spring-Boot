package fact.it.springbootapi.dto;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnomalyResponse {
    private Integer id;
    private OffsetDateTime timestamp;
    private Double longitude;
    private Double latitude;
    private Integer anomalyTypeId;
    private Integer trainId;
    private Integer countryId;
    private Integer trainTrackId;
    private S3ObjectInputStream photo;
    private Boolean isFixed;
    private Boolean isFalse;
    private Integer count;
}