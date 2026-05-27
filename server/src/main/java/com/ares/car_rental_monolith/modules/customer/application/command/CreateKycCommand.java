package com.ares.car_rental_monolith.modules.customer.application.command;

import com.ares.car_rental_monolith.shared.error.DomainException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

// Input đã validate cho việc tạo 1 hồ sơ KYC mới của customer. File đến từ
// multipart đã được controller chuyển sang byte[] + metadata để command vẫn
// thuần (không phụ thuộc Spring/MultipartFile).
public record CreateKycCommand(
        UUID customerId,
        String legalName,
        String documentType,
        String documentNumber,
        LocalDate issuedDate,
        String issuedPlace,
        FileInput frontFile,
        FileInput backFile,
        FileInput selfieFile,
        List<FileInput> otherFiles
) {

    public record FileInput(byte[] content, String originalFilename, String contentType) {}

    private static final Set<String> VALID_DOC_TYPES =
            Set.of("NATIONAL_ID", "PASSPORT", "DRIVING_LICENSE");

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "webp", "pdf");

    // 10MB / file. Khớp với spring.servlet.multipart.max-file-size để Spring và
    // command cùng từ chối ở cùng ngưỡng — không có chỗ nào file lọt qua được
    // ngưỡng kia.
    private static final long MAX_FILE_BYTES = 10L * 1024 * 1024;

    public static CreateKycCommand from(
            UUID customerId,
            String legalName,
            String documentType,
            String documentNumber,
            LocalDate issuedDate,
            String issuedPlace,
            FileInput frontFile,
            FileInput backFile,
            FileInput selfieFile,
            List<FileInput> otherFiles
    ) {
        if (customerId == null) {
            throw DomainException.validation("customerId is required");
        }
        requireText(legalName, "legalName");
        requireText(documentNumber, "documentNumber");

        String docType = trimToNull(documentType);
        if (docType == null || !VALID_DOC_TYPES.contains(docType)) {
            throw DomainException.validation(
                    "documentType must be one of NATIONAL_ID/PASSPORT/DRIVING_LICENSE");
        }

        validateFile(frontFile, "frontFile");
        validateFile(backFile, "backFile");
        validateFile(selfieFile, "selfieFile");

        List<FileInput> others = new ArrayList<>();
        if (otherFiles != null) {
            int i = 0;
            for (FileInput f : otherFiles) {
                if (f == null) continue;
                validateFile(f, "otherFiles[" + i + "]");
                others.add(f);
                i++;
            }
        }

        return new CreateKycCommand(
                customerId,
                legalName.trim(),
                docType,
                documentNumber.trim(),
                issuedDate,
                trimToNull(issuedPlace),
                frontFile,
                backFile,
                selfieFile,
                List.copyOf(others)
        );
    }

    private static void validateFile(FileInput f, String field) {
        if (f == null) return;
        if (f.content() == null || f.content().length == 0) {
            throw DomainException.validation(field + " is empty");
        }
        if (f.content().length > MAX_FILE_BYTES) {
            throw DomainException.validation(field + " exceeds 10MB limit");
        }
        String ext = extensionOf(f.originalFilename());
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw DomainException.validation(
                    field + " must be one of: " + ALLOWED_EXTENSIONS);
        }
        String ct = f.contentType();
        boolean okContentType = ct != null && (ct.startsWith("image/") || ct.equals("application/pdf"));
        if (!okContentType) {
            throw DomainException.validation(
                    field + " content-type must be image/* or application/pdf");
        }
    }

    public static String extensionOf(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) return "";
        return filename.substring(dot + 1).toLowerCase();
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw DomainException.validation(field + " is required");
        }
    }

    private static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
