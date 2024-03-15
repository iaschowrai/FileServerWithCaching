package com.iaschowrai.fileserver.repository;

import com.iaschowrai.fileserver.model.FileServer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileServerRepository extends JpaRepository<FileServer, Long> {

}
