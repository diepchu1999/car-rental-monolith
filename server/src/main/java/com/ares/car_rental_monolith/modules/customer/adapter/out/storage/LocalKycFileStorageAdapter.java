package com.ares.car_rental_monolith.modules.customer.adapter.out.storage;

import com.ares.car_rental_monolith.modules.customer.application.port.out.KycFileStoragePort;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// Lưu file KYC ra filesystem theo cây thư mục {baseDir}/{customerId}/{kycId}/.
// Tách riêng khỏi vehicle image storage để (a) đường base dir + URL khác nhau,
// (b) rule kiểm tra/ retention sau này có thể khác (vd KYC cần purge sau X năm).
@Component
class LocalKycFileStorageAdapter implements KycFileStoragePort {

    private static final String PUBLIC_PATH_PREFIX = "/media/kyc-documents";

    private final Path baseDir;
    // URL prefix tuyệt đối (vd "http://localhost:8080"). FE chạy origin khác
    // (Vite 5173) nên fileUrl phải là URL đầy đủ để <img src> trỏ thẳng về BE
    // không bị Vite "nuốt" thành 404. Để rỗng nếu muốn lưu URL root-relative
    // (khi BE + FE cùng origin trong production sau reverse-proxy).
    private final String publicBaseUrl;

    LocalKycFileStorageAdapter(
            @Value("${app.media.kyc-documents-dir}") String dir,
            @Value("${app.media.public-base-url:}") String publicBaseUrl) {
        this.baseDir = Path.of(dir).toAbsolutePath().normalize();
        this.publicBaseUrl = stripTrailingSlash(publicBaseUrl);
    }

    private static String stripTrailingSlash(String value) {
        if (value == null) return "";
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    @PostConstruct
    void ensureDirectory() {
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create KYC documents directory: " + baseDir, e);
        }
    }

    @Override
    public String store(UUID customerId, UUID kycId, String side, byte[] content, String extension) {
        Path dir = baseDir.resolve(customerId.toString()).resolve(kycId.toString()).normalize();
        // Chống path traversal: dir đã resolve phải nằm trong baseDir.
        if (!dir.startsWith(baseDir)) {
            throw new IllegalArgumentException("Resolved KYC path escapes base dir: " + dir);
        }
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create KYC sub-directory: " + dir, e);
        }

        String prefix = side == null ? "file" : side.toLowerCase(Locale.ROOT);
        String filename = prefix + "-" + UUID.randomUUID()
                + (extension == null || extension.isBlank() ? "" : "." + extension);
        Path target = dir.resolve(filename).normalize();
        try {
            Files.write(target, content);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot store KYC file: " + filename, e);
        }
        return publicBaseUrl + PUBLIC_PATH_PREFIX + "/" + customerId + "/" + kycId + "/" + filename;
    }
}
