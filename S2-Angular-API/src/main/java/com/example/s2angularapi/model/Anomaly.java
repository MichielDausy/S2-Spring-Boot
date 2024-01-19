package com.example.s2angularapi.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

import java.time.OffsetDateTime;

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
    private OffsetDateTime timestamp;
    @Column(columnDefinition = "Geometry(Point,4326)")
    private Point anomalyLocation;
    private String photo;
    private boolean isFixed;
    private boolean isFalse;
    @ManyToOne
    @JoinColumn(name="trainId")
    @JsonBackReference
    private Train train;
    @ManyToOne
    @JoinColumn(name="typeId")
    @JsonBackReference
    private AnomalyType anomalyType;
    @ManyToOne
    @JoinColumn(name="signId")
    @JsonBackReference
    private Sign sign;
}
