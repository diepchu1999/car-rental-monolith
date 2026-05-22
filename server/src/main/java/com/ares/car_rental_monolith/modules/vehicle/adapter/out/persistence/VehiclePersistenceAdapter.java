package com.ares.car_rental_monolith.modules.vehicle.adapter.out.persistence;

import com.ares.car_rental_monolith.modules.vehicle.application.port.out.LoadVehiclePort;
import com.ares.car_rental_monolith.modules.vehicle.application.query.ListVehiclesQuery;
import com.ares.car_rental_monolith.modules.vehicle.application.query.PageVehiclesQuery;
import com.ares.car_rental_monolith.modules.vehicle.application.view.VehicleDetail;
import com.ares.car_rental_monolith.modules.vehicle.domain.Vehicle;
import com.ares.car_rental_monolith.shared.api.PageResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
class VehiclePersistenceAdapter implements LoadVehiclePort {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    private final VehicleJpaRepository repository;
    private final CustomerNameQuery customerNameQuery;

    VehiclePersistenceAdapter(
            VehicleJpaRepository repository,
            CustomerNameQuery customerNameQuery
    ) {
        this.repository = repository;
        this.customerNameQuery = customerNameQuery;
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
    public PageResponse<Vehicle> loadVehiclePage(PageVehiclesQuery query) {
        Page<VehicleJpaEntity> page = repository.search(
                query.q(),
                query.sourceCode(),
                query.statusCode(),
                PageRequest.of(query.pageIndex(), query.size())
        );

        return PageResponse.of(
                page.getContent().stream().map(VehiclePersistenceMapper::toDomain).toList(),
                page.getTotalElements(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );
    }

    @Override
    public Optional<VehicleDetail> loadVehicleDetail(UUID vehicleId) {
        return repository.findById(vehicleId)
                .map(VehiclePersistenceMapper::toDomain)
                .map(this::enrichWithOwnerName);
    }

    private VehicleDetail enrichWithOwnerName(Vehicle vehicle) {
        String ownerName = customerNameQuery.findFullName(vehicle.ownerCustomerId()).orElse(null);
        return VehicleDetail.of(vehicle, ownerName);
    }
}
