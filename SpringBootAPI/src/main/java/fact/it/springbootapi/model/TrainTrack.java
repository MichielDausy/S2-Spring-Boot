package fact.it.springbootapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private Long id;
    private String name;
    @OneToMany(mappedBy = "trainTrack")
    private List<Anomaly> anomalies;
}
