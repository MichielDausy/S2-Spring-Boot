package com.example.s2angularapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnomalyRequest {
    private OffsetDateTime timestamp;
    private String longitude;
    private String latitude;
    private String anomalyType;
    private String sign;
    private String train;
}
