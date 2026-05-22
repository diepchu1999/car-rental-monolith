package com.ares.car_rental_monolith.modules.location.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ares.car_rental_monolith.modules.location.application.command.ImportAdministrativeUnitRecord;
import com.ares.car_rental_monolith.modules.location.application.port.out.WriteAdministrativeUnitPort;
import com.ares.car_rental_monolith.shared.error.DomainException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdministrativeUnitImportServiceTest {

    @Mock
    private WriteAdministrativeUnitPort port;

    @InjectMocks
    private AdministrativeUnitImportService service;

    @Captor
    private ArgumentCaptor<List<ImportAdministrativeUnitRecord>> captor;

    private static ImportAdministrativeUnitRecord province(String code) {
        return new ImportAdministrativeUnitRecord(
                code, "Tỉnh " + code, null, "PROVINCE", "PROVINCE", null, null, null, null);
    }

    private static ImportAdministrativeUnitRecord commune(String code, String parent) {
        return new ImportAdministrativeUnitRecord(
                code, "Xã " + code, null, "COMMUNE", "WARD", parent, null, null, null);
    }

    @Test
    void upsertsValidRecordsAndAppliesDefaults() {
        when(port.upsertAll(anyList())).thenReturn(2);

        int written = service.handle(List.of(province("01"), commune("00004", "01")));

        assertThat(written).isEqualTo(2);
    }

    @Test
    void appliesDefaultEffectiveFromAndStatus() {
        when(port.upsertAll(captor.capture())).thenReturn(1);

        service.handle(List.of(province("01")));

        ImportAdministrativeUnitRecord normalized = captor.getValue().get(0);
        assertThat(normalized.effectiveFrom()).isEqualTo(LocalDate.of(2025, 7, 1));
        assertThat(normalized.status()).isEqualTo("ACTIVE");
    }

    @Test
    void rejectsEmptyInput() {
        assertThatThrownBy(() -> service.handle(List.of()))
                .isInstanceOf(DomainException.class);
        verifyNoInteractions(port);
    }

    @Test
    void rejectsMissingCode() {
        ImportAdministrativeUnitRecord bad = new ImportAdministrativeUnitRecord(
                " ", "Tỉnh", null, "PROVINCE", "PROVINCE", null, null, null, null);
        assertThatThrownBy(() -> service.handle(List.of(bad)))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("code");
    }

    @Test
    void rejectsInvalidLevel() {
        ImportAdministrativeUnitRecord bad = new ImportAdministrativeUnitRecord(
                "01", "X", null, "DISTRICT", "PROVINCE", null, null, null, null);
        assertThatThrownBy(() -> service.handle(List.of(bad)))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("level");
    }

    @Test
    void rejectsCommuneWithoutParent() {
        ImportAdministrativeUnitRecord bad = commune("00004", null);
        assertThatThrownBy(() -> service.handle(List.of(bad)))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("parentCode");
    }

    @Test
    void rejectsProvinceWithParent() {
        ImportAdministrativeUnitRecord bad = new ImportAdministrativeUnitRecord(
                "01", "X", null, "PROVINCE", "PROVINCE", "99", null, null, null);
        assertThatThrownBy(() -> service.handle(List.of(bad)))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("parentCode");
    }
}
