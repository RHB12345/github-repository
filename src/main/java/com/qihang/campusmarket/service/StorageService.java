package com.qihang.campusmarket.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

@Service
public class StorageService {
    private final Path uploadRoot;

    public StorageService(@Value("${campus.upload-dir:uploads}") String uploadDir) {
        this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public String storeProductImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IllegalArgumentException("只能上传图片文件");
        }
        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "image" : file.getOriginalFilename());
        String extension = "";
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0 && dot < originalName.length() - 1) {
            extension = originalName.substring(dot).toLowerCase(Locale.ROOT);
        }
        String filename = UUID.randomUUID() + extension;
        Path productDir = uploadRoot.resolve("products");
        Files.createDirectories(productDir);
        Files.copy(file.getInputStream(), productDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/products/" + filename;
    }

    public String defaultImage(String category) {
        return switch (category) {
            case "教材资料" -> "/images/products/book.svg";
            case "数码电子" -> "/images/products/device.svg";
            case "生活用品" -> "/images/products/life.svg";
            case "运动户外" -> "/images/products/sport.svg";
            default -> "/images/products/other.svg";
        };
    }
}
