package com.example.s2angularapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnomalyResponse {
    private OffsetDateTime timestamp;
    private Point location;
    private String photo;
    private String anomalyType;
    private String sign;
    private String train;
}
