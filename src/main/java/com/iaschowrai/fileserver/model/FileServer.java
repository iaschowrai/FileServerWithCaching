package com.iaschowrai.fileserver.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileServer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private Long id;
    private String fileName;
    private String uploadFileName;


    @CreationTimestamp
    private LocalDateTime createdTime;
    private String fileExtension;
}
