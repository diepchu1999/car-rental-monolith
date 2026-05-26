package com.ares.car_rental_monolith.modules.vehicle.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ares.car_rental_monolith.modules.location.api.AdministrativeUnitDirectory;
import com.ares.car_rental_monolith.modules.location.api.AdministrativeUnitRef;
import com.ares.car_rental_monolith.modules.vehicle.application.command.UpdateListingCommand;
import com.ares.car_rental_monolith.modules.vehicle.application.port.out.LoadVehiclePort;
import com.ares.car_rental_monolith.modules.vehicle.application.port.out.WriteVehiclePort;
import com.ares.car_rental_monolith.modules.vehicle.application.view.VehicleDetail;
import com.ares.car_rental_monolith.modules.vehicle.domain.Vehicle;
import com.ares.car_rental_monolith.shared.error.DomainException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehicleUpdateServiceLocationTest {

    private static final UUID VEHICLE_ID = UUID.randomUUID();

    @Mock
    private WriteVehiclePort writePort;
    @Mock
    private LoadVehiclePort loadPort;
    @Mock
    private AdministrativeUnitDirectory administrativeUnits;

    @InjectMocks
    private VehicleUpdateService service;

    private static AdministrativeUnitRef province(String code) {
        return new AdministrativeUnitRef(code, "Tỉnh " + code, "Tỉnh " + code, "PROVINCE", "PROVINCE", null);
    }

    private static AdministrativeUnitRef commune(String code, String parent) {
        return new AdministrativeUnitRef(code, "Xã " + code, "Xã " + code, "COMMUNE", "WARD", parent);
    }

    private UpdateListingCommand command(String provinceCode, String communeCode) {
        return UpdateListingCommand.from(
                VEHICLE_ID, "Title", "Desc", provinceCode, communeCode, "12 Phố Huế",
                null, "VND", null, null);
    }

    @Test
    void validAddressResolvesNamesAndWrites() {
        when(writePort.findVehicle(VEHICLE_ID)).thenReturn(Optional.of(mock(Vehicle.class)));
        when(administrativeUnits.findActiveByCode("01")).thenReturn(Optional.of(province("01")));
        when(administrativeUnits.findActiveByCode("00004")).thenReturn(Optional.of(commune("00004", "01")));
        VehicleDetail detail = mock(VehicleDetail.class);
        when(loadPort.loadVehicleDetail(VEHICLE_ID)).thenReturn(Optional.of(detail));

        VehicleDetail result = service.handle(command("01", "00004"));

        assertThat(result).isSameAs(detail);
        ArgumentCaptor<UpdateListingCommand> captor = ArgumentCaptor.forClass(UpdateListingCommand.class);
        verify(writePort).updateListingDraft(captor.capture());
        UpdateListingCommand written = captor.getValue();
        assertThat(written.provinceName()).isEqualTo("Tỉnh 01");
        assertThat(written.communeName()).isEqualTo("Xã 00004");
        assertThat(written.provinceCode()).isEqualTo("01");
        assertThat(written.communeCode()).isEqualTo("00004");
    }

    @Test
    void rejectsCommuneNotBelongingToProvince() {
        when(writePort.findVehicle(VEHICLE_ID)).thenReturn(Optional.of(mock(Vehicle.class)));
        when(administrativeUnits.findActiveByCode("01")).thenReturn(Optional.of(province("01")));
        when(administrativeUnits.findActiveByCode("00004")).thenReturn(Optional.of(commune("00004", "99")));

        assertThatThrownBy(() -> service.handle(command("01", "00004")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("does not belong to province");

        verify(writePort, never()).updateListingDraft(any());
    }

    @Test
    void rejectsInactiveProvince() {
        when(writePort.findVehicle(VEHICLE_ID)).thenReturn(Optional.of(mock(Vehicle.class)));
        when(administrativeUnits.findActiveByCode("01")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.handle(command("01", "00004")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("provinceCode");

        verify(writePort, never()).updateListingDraft(any());
    }
}
