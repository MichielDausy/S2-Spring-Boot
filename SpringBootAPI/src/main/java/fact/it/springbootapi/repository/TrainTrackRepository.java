package fact.it.springbootapi.repository;

import fact.it.springbootapi.model.TrainTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainTrackRepository extends JpaRepository<TrainTrack, Integer> {
    @Query("SELECT ST_ASTEXT(t.trackGeometry) FROM TrainTrack t WHERE t.id = :trackId")
    String findTrackGeometryAsText(@Param("trackId") Long trackId);
}
