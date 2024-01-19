package com.example.s2angularapi.service;

import com.example.s2angularapi.Exception.StorageException;
import com.example.s2angularapi.Interface.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
@Transactional
public class FileSystemStorageService implements StorageService {
    @Value("${rootLocation.path}")
    private Path rootLocation;

    public String store(MultipartFile file, String timestamp) {
        // Normalize file name
        String fileName =  timestamp + StringUtils.cleanPath(file.getOriginalFilename());
        try {
            // Check if the file's name contains valid  characters or not
            if (fileName.contains("..")) {
                throw new RuntimeException("Sorry! File name which contains invalid path sequence " + fileName);
            }
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file.");
            }
            fileName = fileName.replace(':', '.');

            // Copy file to the target place (Replacing existing file with the same name)
            Path targetLocation = this.rootLocation.resolve(
                            Paths.get(fileName))
                    .normalize().toAbsolutePath();

            if (!targetLocation.getParent().equals(this.rootLocation.toAbsolutePath())) {
                // This is a security check
                throw new StorageException("Cannot store file outside current directory.");
            }

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileName;
    }
}
