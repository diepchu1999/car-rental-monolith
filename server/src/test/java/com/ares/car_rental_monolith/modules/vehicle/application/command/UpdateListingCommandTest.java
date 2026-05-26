package com.ares.car_rental_monolith.modules.vehicle.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ares.car_rental_monolith.shared.error.DomainException;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UpdateListingCommandTest {

    private static final UUID VEHICLE_ID = UUID.randomUUID();

    @Test
    void buildsWithProvinceAndCommune_withoutAnyDistrict() {
        // The new address model has no district field at all — a valid command is
        // built from province/commune codes only.
        UpdateListingCommand command = UpdateListingCommand.from(
                VEHICLE_ID, "Title", "Desc", "01", "00004", "12 Phố Huế",
                new BigDecimal("500000"), "VND", true, false);

        assertThat(command.provinceCode()).isEqualTo("01");
        assertThat(command.communeCode()).isEqualTo("00004");
        assertThat(command.pickupAddress()).isEqualTo("12 Phố Huế");
    }

    @Test
    void requiresProvinceCode() {
        assertThatThrownBy(() -> UpdateListingCommand.from(
                VEHICLE_ID, "Title", null, "  ", "00004", null,
                null, null, null, null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("provinceCode");
    }

    @Test
    void requiresCommuneCode() {
        assertThatThrownBy(() -> UpdateListingCommand.from(
                VEHICLE_ID, "Title", null, "01", null, null,
                null, null, null, null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("communeCode");
    }

    @Test
    void rejectsNegativeRate() {
        assertThatThrownBy(() -> UpdateListingCommand.from(
                VEHICLE_ID, "Title", null, "01", "00004", null,
                new BigDecimal("-1"), null, null, null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("baseDailyRate");
    }

    @Test
    void withResolvedLocationAttachesNames() {
        UpdateListingCommand resolved = UpdateListingCommand.from(
                VEHICLE_ID, "Title", null, "01", "00004", null,
                null, null, null, null)
                .withResolvedLocation("Thành phố Hà Nội", "Phường Ba Đình");

        assertThat(resolved.provinceName()).isEqualTo("Thành phố Hà Nội");
        assertThat(resolved.communeName()).isEqualTo("Phường Ba Đình");
        assertThat(resolved.provinceCode()).isEqualTo("01");
        assertThat(resolved.communeCode()).isEqualTo("00004");
    }
}
