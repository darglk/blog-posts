package com.darglk.blogposts.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MockFileServiceImpl implements FileService {

    @Override
    public String uploadFile(MultipartFile file) {
        log.info("Mocked upload file: {}", file.getName());
        return UUID.randomUUID().toString();
    }

    @Override
    public List<String> uploadFiles(List<MultipartFile> files) {
        return files.stream().map(this::uploadFile).collect(Collectors.toList());
    }

    @Override
    public void deleteFile(String url) {
        log.info("Deleting file: {}", url);
    }
}
