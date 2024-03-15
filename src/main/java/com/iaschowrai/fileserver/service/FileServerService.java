package com.iaschowrai.fileserver.service;

import com.iaschowrai.fileserver.dto.FileUploadResponse;
import com.iaschowrai.fileserver.model.FileServer;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;

public interface FileServerService {

    FileUploadResponse uploadFile(MultipartFile file);
    ByteArrayResource downloadFile(String  filename);
}
