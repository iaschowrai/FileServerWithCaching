package com.iaschowrai.fileserver.service;
//
//import com.iaschowrai.fileserver.dto.FileUploadResponse;
//import com.iaschowrai.fileserver.model.FileServer;
//import com.iaschowrai.fileserver.repository.FileServerRepository;
//import jakarta.persistence.EntityNotFoundException;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cache.annotation.CacheEvict;
//import org.springframework.cache.annotation.Cacheable;
//import org.springframework.core.io.ByteArrayResource;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import javax.annotation.PostConstruct;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.StandardCopyOption;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class FileServerServiceImpl implements FileServerService {
//
//    @Value("${file.upload.path}")
//    private String uploadPath;
//
//    private final FileServerRepository fileServerRepository;
//    private final Map<String, byte[]> fileCache = new HashMap<>();
//
//    @PostConstruct
//    public void init() {
//        // Populate the file cache on startup
//        loadFilesIntoCache();
//    }
//
//    @Cacheable("files")
//    public void loadFilesIntoCache() {
//        // Load files from the database or file system into the cache
//        // Example: fetch files from the database and store them in the cache
//        fileServerRepository.findAll().forEach(file -> {
//            try {
//                String filePath = uploadPath + "/" + file.getUploadFileName();
//                fileCache.put(file.getUploadFileName(), Files.readAllBytes(Path.of(filePath)));
//            } catch (IOException e) {
//                log.error("Failed to load file {} into cache: {}", file.getUploadFileName(), e.getMessage());
//            }
//        });
//    }
//
//    @Override
//    @CacheEvict(value = "files", allEntries = true)
//    public FileUploadResponse uploadFile(MultipartFile file) {
//        try {
//            if (!Files.exists(Path.of(uploadPath))) {
//                // Create an upload folder if it doesn't exist
//                Files.createDirectories(Path.of(uploadPath));
//            }
//
//            // Original file name
//            String originalFilename = file.getOriginalFilename();
//
//            // File extension
//            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
//
//            // Upload file name with UUID
//            String uploadFilename = UUID.randomUUID() + fileExtension;
//
//            // Save file metadata to the database
//            FileServer fileMetadata = FileServer.builder()
//                    .fileName(originalFilename)
//                    .fileExtension(fileExtension)
//                    .uploadFileName(uploadFilename)
//                    .build();
//            fileServerRepository.save(fileMetadata);
//
//            // Save file to the upload path
//            String targetPath = uploadPath + "/" + uploadFilename;
//            Files.copy(file.getInputStream(), Path.of(targetPath), StandardCopyOption.REPLACE_EXISTING);
//
//            // Update the file cache
//            fileCache.put(uploadFilename, file.getBytes());
//
//            // Return the file upload response
//            return FileUploadResponse.builder()
//                    .fileName(uploadFilename)
//                    .downloadUrl("/api/files/download/" + uploadFilename)
//                    .build();
//        } catch (IOException e) {
//            log.error("Failed to upload file: {}", e.getMessage());
//            throw new EntityNotFoundException("Unable to upload file");
//        }
//    }
//
//    @Override
//    @Cacheable("files")
//    public ByteArrayResource downloadFile(String filename) {
//        log.info("[downloadFile] - START");
//
//        // Check if the file is present in the cache
//        byte[] fileBytes = fileCache.get(filename);
//        if (fileBytes == null) {
//            // If not found in cache, fetch file from the file system or database
//            FileServer fileMetadata = fileServerRepository.findByUploadFileName(filename)
//                    .orElseThrow(() -> new EntityNotFoundException("File not found: " + filename));
//            String filePath = uploadPath + "/" + fileMetadata.getUploadFileName();
//            try {
//                fileBytes = Files.readAllBytes(Path.of(filePath));
//            } catch (IOException e) {
//                log.error("Failed to read file {}: {}", filename, e.getMessage());
//                throw new EntityNotFoundException("Unable to fetch file: " + filename);
//            }
//        }
//
//        log.info("[downloadFile] - END");
//        return new ByteArrayResource(fileBytes);
//    }
//}



import com.iaschowrai.fileserver.dto.FileUploadResponse;
import com.iaschowrai.fileserver.model.FileServer;
import com.iaschowrai.fileserver.repository.FileServerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileServerServiceImpl implements FileServerService {

    @Value("${file.upload.path}")
    private String uploadPath;

    private final FileServerRepository fileServerRepository;
    private final Map<String, byte[]> fileCache = new HashMap<>();

    @PostConstruct
    public void init() {
        // Populate the file cache asynchronously on startup
        loadFilesIntoCacheAsync();
    }

    @Async
    public void loadFilesIntoCacheAsync() {
        // Fetch all file metadata from the repository
        List<FileServer> fileMetadataList = fileServerRepository.findAll();

        // Partition the list into smaller batches for concurrent processing
        List<List<FileServer>> batches = partitionList(fileMetadataList, 10); // Adjust batch size as needed

        // Process each batch asynchronously
        List<CompletableFuture<Void>> futures = batches.stream()
                .map(this::loadBatchFilesIntoCacheAsync)
                .collect(Collectors.toList());

        // Wait for all asynchronous tasks to complete
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            allOf.get(); // Wait for all tasks to complete
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error loading files into cache asynchronously: {}", e.getMessage());
            // Handle exception
        }
    }

    private List<List<FileServer>> partitionList(List<FileServer> list, int batchSize) {
        return IntStream.range(0, (list.size() + batchSize - 1) / batchSize)
                .mapToObj(i -> list.subList(i * batchSize, Math.min((i + 1) * batchSize, list.size())))
                .collect(Collectors.toList());
    }

    private CompletableFuture<Void> loadBatchFilesIntoCacheAsync(List<FileServer> batch) {
        return CompletableFuture.runAsync(() -> {
            batch.forEach(file -> {
                try {
                    String filePath = uploadPath + "/" + file.getUploadFileName();
                    byte[] fileContent = Files.readAllBytes(Path.of(filePath));
                    fileCache.put(file.getUploadFileName(), fileContent);
                } catch (IOException e) {
                    log.error("Failed to load file {} into cache: {}", file.getUploadFileName(), e.getMessage());
                }
            });
        });
    }

    @Override
    @CacheEvict(value = "files", allEntries = true)

    public FileUploadResponse uploadFile(MultipartFile file) {
        try {
            if (!Files.exists(Path.of(uploadPath))) {
                Files.createDirectories(Path.of(uploadPath));
            }

            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uploadFilename = UUID.randomUUID() + fileExtension;

            FileServer fileMetadata = FileServer.builder()
                    .fileName(originalFilename)
                    .fileExtension(fileExtension)
                    .uploadFileName(uploadFilename)
                    .build();
            fileServerRepository.save(fileMetadata);

            String targetPath = uploadPath + "/" + uploadFilename;
            Files.copy(file.getInputStream(), Path.of(targetPath), StandardCopyOption.REPLACE_EXISTING);

            fileCache.put(uploadFilename, file.getBytes());

            return FileUploadResponse.builder()
                    .fileName(uploadFilename)
                    .downloadUrl("/api/files/download/" + uploadFilename)
                    .build();
        } catch (IOException e) {
            log.error("Failed to upload file: {}", e.getMessage());
            throw new EntityNotFoundException("Unable to upload file");
        }
    }

    @Override
    @Cacheable("files")
    public ByteArrayResource downloadFile(String filename) {
        log.info("[downloadFile] - START");

        byte[] fileBytes = fileCache.get(filename);
        if (fileBytes == null) {
            FileServer fileMetadata = fileServerRepository.findByUploadFileName(filename)
                    .orElseThrow(() -> new EntityNotFoundException("File not found: " + filename));
            String filePath = uploadPath + "/" + fileMetadata.getUploadFileName();
            try {
                fileBytes = Files.readAllBytes(Path.of(filePath));
            } catch (IOException e) {
                log.error("Failed to read file {}: {}", filename, e.getMessage());
                throw new EntityNotFoundException("Unable to fetch file: " + filename);
            }
        }

        log.info("[downloadFile] - END");
        return new ByteArrayResource(fileBytes);
    }
}
