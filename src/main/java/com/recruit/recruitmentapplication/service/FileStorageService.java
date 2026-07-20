package com.recruit.recruitmentapplication.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Lưu file tải lên (CV) vào một thư mục cục bộ trên đĩa.
 * Tên file gốc được giữ lại trong ApplicationDocument, còn tên file thật trên đĩa
 * dùng UUID để tránh trùng lặp / ghi đè.
 */
@Service
public class FileStorageService {
    private final Path root;

    public FileStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException ex) {
            throw new UncheckedIOException("Không thể khởi tạo thư mục lưu trữ file: " + root, ex);
        }
    }

    /**
     * Lưu file vào {subfolder} dưới thư mục gốc, trả về đường dẫn tương đối để lưu vào DB.
     */
    public String store(MultipartFile file, String subfolder) {
        try {
            Path targetDir = root.resolve(subfolder).normalize();
            Files.createDirectories(targetDir);
            String extension = extensionOf(file.getOriginalFilename());
            String storedFileName = UUID.randomUUID() + extension;
            Path targetFile = targetDir.resolve(storedFileName);
            file.transferTo(targetFile);
            return subfolder + "/" + storedFileName;
        } catch (IOException ex) {
            throw new UncheckedIOException("Không thể lưu file tải lên", ex);
        }
    }

    private String extensionOf(String originalFileName) {
        if (originalFileName == null) {
            return "";
        }
        int dotIndex = originalFileName.lastIndexOf('.');
        return dotIndex < 0 ? "" : originalFileName.substring(dotIndex);
    }
}
