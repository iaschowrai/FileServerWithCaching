package com.iaschowrai.fileserver.service;

import com.iaschowrai.fileserver.dto.FileUploadResponse;
import com.iaschowrai.fileserver.model.FileServer;
import com.iaschowrai.fileserver.repository.FileServerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InaccessibleObjectException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileServerServiceImpl implements FileServerService{

    @Value("${file.upload.path}")
    public String uploadPath;


    private final FileServerRepository fileServerRepository;

    @Override
    public FileUploadResponse uploadFile(MultipartFile file) {

        try{
            if(!Files.exists(Path.of(uploadPath))){
                // create a upload folder
                Files.createDirectories(Path.of(uploadPath));
            }

            // Original file
            //image.png - > .png

            var originalFilename = file.getOriginalFilename();

            // File extension
            var fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));

            // upload file name
            var uploadFilename = UUID.randomUUID() + fileExtension;

            // create meta-data object

            var fileMetadata = FileServer.builder()
                    .fileName(originalFilename)
                    .fileExtension(fileExtension)
                    .uploadFileName(uploadFilename)
                    .build();

            // save file meta-data

            fileServerRepository.save(fileMetadata);

            //save file in local storage
            // .uploads/uploadFilename.extension
            var targetPath = uploadPath +"/" +uploadFilename;

            // copy the file to target location
            Files.copy(file.getInputStream(), Path.of(targetPath) , StandardCopyOption.REPLACE_EXISTING);

            // Return the file upload response

            return FileUploadResponse.builder()
                    .fileName(uploadFilename)
                    .downloadUrl("/api/files/download/" + uploadFilename)
                    .build();


        }catch (Exception e){
            throw new EntityNotFoundException();
        }
    }

    @Override
    @Cacheable("files")
    public ByteArrayResource downloadFile(String filename) {
        log.info("[downloadFile] - START");

        // Fetch file meta-data by filename
        var fileMetadata = fileServerRepository.findByUploadFileName(filename).orElseThrow();

        // create resource Path
        var resourcePath = uploadPath + "/" + fileMetadata.getUploadFileName();

        // Fetch file data
        try (var fileInputStream = new FileInputStream(resourcePath)){
            log.info("[downloadFile] - END");
            return new ByteArrayResource(fileInputStream.readAllBytes());

        } catch (IOException e){
            log.info("[downloadFile] - ERROR - {}" , e.getMessage());
            throw new EntityNotFoundException("UnableToFetchFileException");
        }
    }
}
