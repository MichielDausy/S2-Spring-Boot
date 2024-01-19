package fact.it.springbootapi.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import fact.it.springbootapi.Exception.StorageException;
import fact.it.springbootapi.Exception.StorageFileNotFoundException;
import fact.it.springbootapi.Interface.StorageService;
import fact.it.springbootapi.properties.StorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileSystemStorageService implements StorageService {

    private final Path rootLocation;

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {

        if(properties.getLocation().trim().length() == 0){
            throw new StorageException("File upload location cannot be Empty.");
        }

        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
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

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(this.rootLocation::relativize);
        }
        catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }

    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            }
            else {
                throw new StorageFileNotFoundException(
                        "Could not read file: " + filename);

            }
        }
        catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        }
        catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}