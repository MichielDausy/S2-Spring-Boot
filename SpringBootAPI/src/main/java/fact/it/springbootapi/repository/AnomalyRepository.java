package fact.it.springbootapi.repository;

import fact.it.springbootapi.model.Anomaly;
import fact.it.springbootapi.model.AnomalyType;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnomalyRepository extends JpaRepository<Anomaly, Integer> {
    @Query(value = "SELECT time_bucket('1 sec', a.timestamp) AS bucket," +
            "COUNT(a.id) AS number_of_anomalies " +
            "FROM Anomaly a " +
            "GROUP BY bucket " +
            "ORDER BY bucket ASC")
    List<Object[]> getAnomaliesByDay();

    @Query(value = "SELECT DATE_TRUNC('second', a.timestamp) AS bucket," +
            "COUNT(a.id) AS number_of_anomalies " +
            "FROM Anomaly a " +
            "GROUP BY DATE_TRUNC('second', a.timestamp) " +
            "ORDER BY DATE_TRUNC('second', a.timestamp) ASC")
    List<Object[]> getAnomaliesByDaySQL();

    Anomaly findByIdEquals(Integer id);

    @Query(value = "SELECT ST_ASTEXT(a.anomalyLocation) FROM Anomaly a")
    List<String> getAllAnomaliesOnMap();

    @Query(value = "SELECT a FROM Anomaly a WHERE a.anomalyType.name like :anomalyType and a.isFalse = false and a.isFixed = false ORDER BY ST_DISTANCE(a.anomalyLocation, :anomalyPoint) LIMIT 1")
    Anomaly findClosestAnomaly(@Param("anomalyPoint") Point anomalyPoint, @Param("anomalyType") String anomalyType);

    List<Anomaly> findAllByTrainTrack_Id(Integer trainTrackId);
}