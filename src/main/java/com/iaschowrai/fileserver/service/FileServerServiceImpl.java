package com.iaschowrai.fileserver.service;

import com.iaschowrai.fileserver.dto.FileUploadResponse;
import com.iaschowrai.fileserver.model.FileServer;
import org.springframework.web.multipart.MultipartFile;

public class FileServerServiceImpl implements FileServerService{


    @Override
    public FileUploadResponse uploadFile(MultipartFile file) {
        return null;
    }

    @Override
    public FileUploadResponse downloadFile(String fileName) {
        return null;
    }
}
