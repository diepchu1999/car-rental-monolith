package com.ares.car_rental_monolith.modules.location.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.ares.car_rental_monolith.modules.location.api.AdministrativeUnitRef;
import com.ares.car_rental_monolith.modules.location.application.port.out.LoadAdministrativeUnitPort;
import com.ares.car_rental_monolith.modules.location.domain.AdministrativeUnit;
import com.ares.car_rental_monolith.shared.error.DomainException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdministrativeUnitQueryServiceTest {

    @Mock
    private LoadAdministrativeUnitPort port;

    @InjectMocks
    private AdministrativeUnitQueryService service;

    @Test
    void listProvincesDelegatesWithProvinceLevel() {
        when(port.listByLevel("PROVINCE")).thenReturn(
                List.of(new AdministrativeUnit("01", "Hà Nội", "TP Hà Nội", "PROVINCE", "CITY", null)));

        List<AdministrativeUnit> result = service.handle();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).code()).isEqualTo("01");
    }

    @Test
    void listCommunesRequiresProvinceCode() {
        assertThatThrownBy(() -> service.handle("  "))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("provinceCode");
    }

    @Test
    void searchRejectsInvalidLevel() {
        assertThatThrownBy(() -> service.handle("ha", "DISTRICT", null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("level");
    }

    @Test
    void findActiveByCodeMapsToRef() {
        when(port.findActiveByCode("00004")).thenReturn(Optional.of(
                new AdministrativeUnit("00004", "Ba Đình", "Phường Ba Đình", "COMMUNE", "WARD", "01")));

        Optional<AdministrativeUnitRef> ref = service.findActiveByCode("00004");

        assertThat(ref).isPresent();
        assertThat(ref.get().parentCode()).isEqualTo("01");
        assertThat(ref.get().level()).isEqualTo("COMMUNE");
    }

    @Test
    void findActiveByCodeReturnsEmptyForBlank() {
        assertThat(service.findActiveByCode(" ")).isEmpty();
    }
}
