package com.example.s2angularapi.controller;

import com.example.s2angularapi.dto.AnomalyRequest;
import com.example.s2angularapi.dto.AnomalyResponse;
import com.example.s2angularapi.service.AnomalyService;
import com.example.s2angularapi.service.FileSystemStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/anomalies")
@RequiredArgsConstructor
public class AnomalyController {
    private final AnomalyService anomalyService;
    private final FileSystemStorageService fileSystemStorageService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<AnomalyResponse> getAnomalies() {
        return anomalyService.getAllAnomalies();
    }

    @PostMapping("/new")
    @ResponseStatus(HttpStatus.OK)
    public AnomalyResponse addAnomaly(@ModelAttribute AnomalyRequest data, @RequestParam("file") MultipartFile file) {
        AnomalyResponse response = null;
        try {
            String fileName = fileSystemStorageService.store(file, data.getTimestamp().toString());
            response = anomalyService.addAnomaly(data, fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
}
