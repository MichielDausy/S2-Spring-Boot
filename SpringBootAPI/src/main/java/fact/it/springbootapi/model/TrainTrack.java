package fact.it.springbootapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.LineString;

import java.util.List;

@Entity
@Table(name = "trainTrack")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrainTrack {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    @Column(columnDefinition = "geometry(LineString,4326)")
    private LineString trackGeometry;
    @OneToMany(mappedBy = "trainTrack")
    private List<Anomaly> anomalies;
}
