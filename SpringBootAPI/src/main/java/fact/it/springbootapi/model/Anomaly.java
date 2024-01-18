package fact.it.springbootapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "anomaly")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Anomaly {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private OffsetDateTime timestamp;
    private String longitude;
    private String latitude;
    private String photo;
    private boolean isFixed;
    private boolean isFalse;
    @ManyToOne
    @JoinColumn(name="trainId")
    private Train train;
    @ManyToOne
    @JoinColumn(name="typeId")
    private AnomalyType anomalyType;
    @ManyToOne
    @JoinColumn(name="signId")
    private Sign sign;
    @ManyToOne
    @JoinColumn(name="countryId")
    private Country country;
    @ManyToOne
    @JoinColumn(name="trainTrackId")
    private TrainTrack trainTrack;
}
