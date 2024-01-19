package com.example.s2angularapi.repository;

import com.example.s2angularapi.model.Anomaly;
import com.example.s2angularapi.model.Sign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
}
