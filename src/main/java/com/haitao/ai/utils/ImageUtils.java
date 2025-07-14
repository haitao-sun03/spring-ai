package com.haitao.ai.utils;

import org.springframework.web.multipart.MultipartFile;

public class ImageUtils {
    private ImageUtils() {}
    public static String convertImageToBase64(MultipartFile image) throws Exception {
        byte[] imageBytes = image.getBytes();
        return java.util.Base64.getEncoder().encodeToString(imageBytes);
    }
}
