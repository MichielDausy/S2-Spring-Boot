package fact.it.springbootapi.repository;

import fact.it.springbootapi.model.TrainTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainTrackRepository extends JpaRepository<TrainTrack, Integer> {
}
