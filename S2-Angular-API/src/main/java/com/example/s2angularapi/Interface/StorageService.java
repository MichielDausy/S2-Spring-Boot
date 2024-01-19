package com.example.s2angularapi.Interface;

import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {
    String store(MultipartFile file, String timestamp);
}