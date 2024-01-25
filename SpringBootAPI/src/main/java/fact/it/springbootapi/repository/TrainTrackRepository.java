package fact.it.springbootapi.repository;

import fact.it.springbootapi.model.TrainTrack;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainTrackRepository extends JpaRepository<TrainTrack, Integer> {
    @Query("SELECT ST_ASTEXT(t.trackGeometry) FROM TrainTrack t WHERE t.id = :trackId")
    String findTrackGeometryAsText(@Param("trackId") Long trackId);

    TrainTrack findByName(String name);

    @Query("SELECT tt FROM TrainTrack tt WHERE INTERSECTS(tt.trackGeometry, :point) = true")
    List<TrainTrack> findByTrackGeometryIntersects(@Param("point") Point point);

    @Query("SELECT tt FROM TrainTrack tt ORDER BY DISTANCE(tt.trackGeometry, :point) ASC LIMIT 1")
    TrainTrack findClosestTrainTrack(@Param("point") Point point);

    @Query("SELECT ST_ASTEXT(t.trackGeometry) FROM TrainTrack t")
    List<String> getAllTraintracks();
}
