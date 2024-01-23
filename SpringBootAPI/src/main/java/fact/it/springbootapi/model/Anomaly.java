package fact.it.springbootapi.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "anomaly")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(value = {"anomalyType", "train", "sign", "country", "trainTrack"}, allowSetters = true)
public class Anomaly {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private OffsetDateTime timestamp;
    @Column(columnDefinition = "geometry(Point,4326)")
    private Point anomalyLocation;
    private String photo;
    private Boolean isFixed;
    private Boolean isFalse;
    @ManyToOne
    @JoinColumn(name="trainId")
    private Train train;
    @ManyToOne
    @JoinColumn(name="typeId")
    private AnomalyType anomalyType;
    @ManyToOne
    @JoinColumn(name="countryId")
    private Country country;
    @ManyToOne
    @JoinColumn(name="trainTrackId")
    private TrainTrack trainTrack;
}
