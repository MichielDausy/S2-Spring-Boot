package fact.it.springbootapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

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
    private Date timestamp;
    private String longitude;
    private String latitude;
    private String photo;
    private boolean isFixed;
    private boolean isFalse;
    @ManyToOne
    @JoinColumn(name="train_id")
    private Train train;
    @ManyToOne
    @JoinColumn(name="type_id")
    private AnomalyType anomalyType;
    @ManyToOne
    @JoinColumn(name="sign_id")
    private Sign sign;
    @ManyToOne
    @JoinColumn(name="country_id")
    private Country country;
    @ManyToOne
    @JoinColumn(name="train_track_id")
    private TrainTrack trainTrack;
}
