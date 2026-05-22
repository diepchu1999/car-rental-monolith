package com.ares.car_rental_monolith.modules.vehicle.adapter.out.persistence;

import com.ares.car_rental_monolith.modules.vehicle.application.port.out.LoadVehiclePort;
import com.ares.car_rental_monolith.modules.vehicle.application.query.ListVehiclesQuery;
import com.ares.car_rental_monolith.modules.vehicle.application.query.PageVehiclesQuery;
import com.ares.car_rental_monolith.modules.vehicle.application.view.VehicleDetail;
import com.ares.car_rental_monolith.modules.vehicle.application.view.VehicleListItem;
import com.ares.car_rental_monolith.modules.vehicle.domain.Vehicle;
import com.ares.car_rental_monolith.shared.api.PageResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
class VehiclePersistenceAdapter implements LoadVehiclePort {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    private final VehicleJpaRepository repository;
    private final VehicleEnrichedListQuery enrichedListQuery;
    private final VehicleDetailQuery detailQuery;

    VehiclePersistenceAdapter(
            VehicleJpaRepository repository,
            VehicleEnrichedListQuery enrichedListQuery,
            VehicleDetailQuery detailQuery
    ) {
        this.repository = repository;
        this.enrichedListQuery = enrichedListQuery;
        this.detailQuery = detailQuery;
    }

    @Override
    public List<Vehicle> loadVehicles(ListVehiclesQuery query) {
        return repository
                .findAll(VehicleJpaSpecifications.from(query), DEFAULT_SORT)
                .stream()
                .map(VehiclePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public PageResponse<VehicleListItem> loadVehicleListPage(PageVehiclesQuery query) {
        return enrichedListQuery.search(query);
    }

    @Override
    public Optional<VehicleDetail> loadVehicleDetail(UUID vehicleId) {
        return detailQuery.load(vehicleId);
    }
}
