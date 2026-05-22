package com.ares.car_rental_monolith.modules.location.adapter.in.cli;

import com.ares.car_rental_monolith.modules.location.application.command.ImportAdministrativeUnitRecord;
import com.ares.car_rental_monolith.modules.location.application.port.in.ImportAdministrativeUnitsUseCase;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

// Seeds the administrative-unit catalog from an official JSON source file.
// Runs only when the property `app.location.import.file` is set, so a normal
// boot never touches it. Usage:
//   mvn spring-boot:run -Dspring-boot.run.arguments=--app.location.import.file=/path/to/administrative-units.json
// or -Dapp.location.import.file=... on the running jar.
//
// Note: the JSON is parsed into an all-String DTO with a plain ObjectMapper, so
// the runner depends on no Jackson bean or date module — robust regardless of the
// app's Jackson auto-configuration. Dates are parsed manually as ISO yyyy-MM-dd.
//
// Expected JSON shape: a top-level array of objects, each with:
//   code, name, fullName, level (PROVINCE|COMMUNE),
//   type (PROVINCE|CITY|COMMUNE|WARD|SPECIAL_ZONE),
//   parentCode (province code for communes; omit for provinces),
//   effectiveFrom (yyyy-MM-dd, optional), effectiveTo (optional), status (optional)
@Component
class AdministrativeUnitImportRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdministrativeUnitImportRunner.class);
    private static final String FILE_ARG = "app.location.import.file";

    private final ImportAdministrativeUnitsUseCase importUseCase;
    private final ObjectMapper objectMapper = new ObjectMapper();

    AdministrativeUnitImportRunner(ImportAdministrativeUnitsUseCase importUseCase) {
        this.importUseCase = importUseCase;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RawUnit(
            String code,
            String name,
            String fullName,
            String level,
            String type,
            String parentCode,
            String effectiveFrom,
            String effectiveTo,
            String status
    ) {}

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String filePath = resolveFilePath(args);
        if (filePath == null || filePath.isBlank()) {
            return;
        }

        Path path = Path.of(filePath);
        if (!Files.isRegularFile(path)) {
            log.error("[location-import] File not found: {}", path.toAbsolutePath());
            return;
        }

        log.info("[location-import] Importing administrative units from {}", path.toAbsolutePath());
        byte[] content = Files.readAllBytes(path);
        RawUnit[] raw = objectMapper.readValue(content, RawUnit[].class);
        List<ImportAdministrativeUnitRecord> records = Arrays.stream(raw)
                .map(AdministrativeUnitImportRunner::toRecord)
                .toList();

        int written = importUseCase.handle(records);
        log.info("[location-import] Imported {} administrative units", written);
    }

    private static ImportAdministrativeUnitRecord toRecord(RawUnit r) {
        return new ImportAdministrativeUnitRecord(
                r.code(), r.name(), r.fullName(), r.level(), r.type(), r.parentCode(),
                parseDate(r.effectiveFrom()), parseDate(r.effectiveTo()), r.status());
    }

    private static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value.trim());
    }

    private static String resolveFilePath(ApplicationArguments args) {
        // Prefer the CLI option (--app.location.import.file=...), fall back to a
        // JVM system property of the same name.
        if (args.containsOption(FILE_ARG)) {
            List<String> values = args.getOptionValues(FILE_ARG);
            if (values != null && !values.isEmpty()) {
                return values.get(0);
            }
        }
        return System.getProperty(FILE_ARG);
    }
}
