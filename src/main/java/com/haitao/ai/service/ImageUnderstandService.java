package com.haitao.ai.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageUnderstandService {

    public String processImage(MultipartFile image, String userQuery);
}
