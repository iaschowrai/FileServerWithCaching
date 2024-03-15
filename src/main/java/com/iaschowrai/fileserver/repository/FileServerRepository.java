package com.iaschowrai.fileserver.repository;

import com.iaschowrai.fileserver.model.FileServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileServerRepository extends JpaRepository<FileServer, Integer> {

    Optional<FileServer> findByUploadFileName(String filename);
}
