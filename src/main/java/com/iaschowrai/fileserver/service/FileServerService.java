package com.iaschowrai.fileserver.service;

import com.iaschowrai.fileserver.dto.FileUploadResponse;
import com.iaschowrai.fileserver.model.FileServer;
import jakarta.transaction.Transactional;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileServerService {

    List<String> getAllFileNames();

    FileUploadResponse uploadFile(MultipartFile file);
    ByteArrayResource downloadFile(String  filename);

    @Transactional
    void deleteFile(String filename);
}
