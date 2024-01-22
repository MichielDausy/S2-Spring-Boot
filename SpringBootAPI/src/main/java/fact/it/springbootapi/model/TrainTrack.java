package fact.it.springbootapi.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;

import java.util.List;

@Entity
@Table(name = "trainTrack")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(value = {"anomalies"}, allowSetters = true)
public class TrainTrack {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    @Column(columnDefinition = "geometry(LineString,4326)")
    private LineString trackGeometry;
    @JsonIgnore
    @OneToMany(mappedBy = "trainTrack")
    private List<Anomaly> anomalies;
}