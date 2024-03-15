package com.iaschowrai.fileserver.controller;

import com.iaschowrai.fileserver.dto.FileUploadResponse;
import com.iaschowrai.fileserver.service.FileServerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/fileserver")
public class FileServerController {
    private final FileServerService fileServerService;

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(@RequestParam(name = "file") MultipartFile file){

        // Check the file is empty
        if(Objects.isNull(file) || file.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(FileUploadResponse
                            .builder()
                            .errorMessage("Invalid File")
                            .build());
        }

        log.info("FileName: {}", file.getOriginalFilename());
        log.info("Content Type: {}", file.getContentType());
        log.info("File Size: {}", file.getSize());

        try{

            var fileUploadResponse = fileServerService.uploadFile(file);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(fileUploadResponse);

        }catch (Exception e){

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(FileUploadResponse.builder()
                            .errorMessage("Unable to Upload file")
                            .build());
        }
    }

}