package fact.it.springbootapi.repository;

import fact.it.springbootapi.model.TrainTrack;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainTrackRepository extends JpaRepository<TrainTrack, Integer> {
    @Query("SELECT ST_ASTEXT(t.trackGeometry) FROM TrainTrack t WHERE t.id = :trackId")
    String findTrackGeometryAsText(@Param("trackId") Long trackId);

    TrainTrack findByName(String name);

    @Query("SELECT tt FROM TrainTrack tt WHERE INTERSECTS(tt.trackGeometry, :point) = true")
    TrainTrack findByTrackGeometryIntersects(@Param("point") Point point);
}
