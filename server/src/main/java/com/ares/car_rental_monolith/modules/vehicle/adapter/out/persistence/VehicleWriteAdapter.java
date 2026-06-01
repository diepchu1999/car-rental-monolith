package com.ares.car_rental_monolith.modules.vehicle.adapter.out.persistence;

import com.ares.car_rental_monolith.modules.vehicle.adapter.out.persistence.entity.VehicleFeatureJpaEntity;
import com.ares.car_rental_monolith.modules.vehicle.adapter.out.persistence.entity.VehicleImageJpaEntity;
import com.ares.car_rental_monolith.modules.vehicle.adapter.out.persistence.entity.VehicleJpaEntity;
import com.ares.car_rental_monolith.modules.vehicle.adapter.out.persistence.entity.VehicleListingJpaEntity;
import com.ares.car_rental_monolith.modules.vehicle.adapter.out.persistence.repository.VehicleFeatureJpaRepository;
import com.ares.car_rental_monolith.modules.vehicle.adapter.out.persistence.repository.VehicleImageJpaRepository;
import com.ares.car_rental_monolith.modules.vehicle.adapter.out.persistence.repository.VehicleJpaRepository;
import com.ares.car_rental_monolith.modules.vehicle.adapter.out.persistence.repository.VehicleListingJpaRepository;
import com.ares.car_rental_monolith.modules.vehicle.application.command.SyncVehicleFeaturesCommand;
import com.ares.car_rental_monolith.modules.vehicle.application.command.SyncVehicleImagesCommand;
import com.ares.car_rental_monolith.modules.vehicle.application.command.UpdateListingCommand;
import com.ares.car_rental_monolith.modules.vehicle.application.command.UpsertPricePlanCommand;
import com.ares.car_rental_monolith.modules.vehicle.application.port.out.WriteVehiclePort;
import com.ares.car_rental_monolith.modules.vehicle.domain.Vehicle;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleListing;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleListingStatus;
import com.ares.car_rental_monolith.shared.sql.SqlLoader;
import jakarta.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class VehicleWriteAdapter implements WriteVehiclePort {

    private final VehicleJpaRepository vehicleRepo;
    private final VehicleListingJpaRepository listingRepo;
    private final VehicleImageJpaRepository imageRepo;
    private final VehicleFeatureJpaRepository featureRepo;
    private final EntityManager em;
    private final SqlLoader sql;

    VehicleWriteAdapter(
            VehicleJpaRepository vehicleRepo,
            VehicleListingJpaRepository listingRepo,
            VehicleImageJpaRepository imageRepo,
            VehicleFeatureJpaRepository featureRepo,
            EntityManager em,
            SqlLoader sql
    ) {
        this.vehicleRepo = vehicleRepo;
        this.listingRepo = listingRepo;
        this.imageRepo = imageRepo;
        this.featureRepo = featureRepo;
        this.em = em;
        this.sql = sql;
    }

    @Override
    public Optional<Vehicle> findVehicle(UUID vehicleId) {
        return vehicleRepo.findById(vehicleId).map(VehiclePersistenceMapper::toDomain);
    }

    @Override
    public Vehicle saveVehicleStatus(Vehicle vehicle) {
        VehicleJpaEntity entity = vehicleRepo.findById(vehicle.id())
                .orElseThrow(() -> new IllegalStateException(
                        "Vehicle vanished during update: " + vehicle.id()));
        entity.setStatus(vehicle.status());
        entity.setUpdatedAt(OffsetDateTime.now());
        return VehiclePersistenceMapper.toDomain(vehicleRepo.save(entity));
    }

    @Override
    public Optional<VehicleListing> findListingByVehicleId(UUID vehicleId) {
        return listingRepo.findByVehicleId(vehicleId).map(VehicleWriteAdapter::toDomain);
    }

    @Override
    public int countListingImages(UUID vehicleId) {
        return (int) imageRepo.countByVehicleId(vehicleId);
    }

    @Override
    public VehicleListing saveListingStatus(VehicleListing listing) {
        VehicleListingJpaEntity entity = listingRepo.findById(listing.id())
                .orElseThrow(() -> new IllegalStateException(
                        "Listing vanished during update: " + listing.id()));
        entity.setStatus(listing.status());
        entity.setPublishedAt(listing.publishedAt());
        entity.setUpdatedAt(OffsetDateTime.now());
        return toDomain(listingRepo.save(entity));
    }

    @Override
    public boolean licensePlateExists(String licensePlate) {
        Number count = (Number) em.createNativeQuery(sql.load(VehicleSqlPaths.LICENSE_PLATE_EXISTS))
                .setParameter("p", licensePlate)
                .getSingleResult();
        return count.longValue() > 0;
    }

    @Override
    public Vehicle createVehicle(Vehicle vehicle) {
        VehicleJpaEntity entity = new VehicleJpaEntity();
        entity.setId(vehicle.id());
        entity.setOwnerCustomerId(vehicle.ownerCustomerId());
        entity.setFleetVehicleId(vehicle.fleetVehicleId());
        entity.setSource(vehicle.source());
        entity.setBrand(vehicle.brand());
        entity.setModel(vehicle.model());
        entity.setVersion(vehicle.version());
        entity.setManufactureYear(vehicle.manufactureYear());
        entity.setLicensePlate(vehicle.licensePlate());
        entity.setSeats(vehicle.seats());
        entity.setTransmission(vehicle.transmission());
        entity.setFuelType(vehicle.fuelType());
        entity.setStatus(vehicle.status());
        entity.setCreatedAt(vehicle.createdAt());
        entity.setUpdatedAt(vehicle.updatedAt());
        return VehiclePersistenceMapper.toDomain(vehicleRepo.save(entity));
    }

    @Override
    public void updateListingDraft(UpdateListingCommand cmd) {
        VehicleListingJpaEntity listing = listingRepo.findByVehicleId(cmd.vehicleId())
                .orElseGet(() -> createDraftListing(cmd.vehicleId()));

        if (cmd.title() != null) listing.setTitle(cmd.title());
        if (cmd.description() != null) listing.setDescription(cmd.description());
        if (cmd.provinceCode() != null) listing.setProvinceCode(cmd.provinceCode());
        if (cmd.communeCode() != null) listing.setCommuneCode(cmd.communeCode());
        // `city` is NOT NULL and still drives the legacy list/filter display, so we
        // keep it populated with the resolved province name. The legacy `district`
        // column is left untouched (read-only for historical rows).
        if (cmd.provinceName() != null) listing.setCity(cmd.provinceName());
        if (cmd.pickupAddress() != null) listing.setPickupAddress(cmd.pickupAddress());
        if (cmd.baseDailyRate() != null) listing.setBaseDailyRate(cmd.baseDailyRate());
        if (cmd.currency() != null) listing.setCurrency(cmd.currency());
        if (cmd.instantBookingEnabled() != null) {
            listing.setInstantBookingEnabled(cmd.instantBookingEnabled());
        }
        if (cmd.deliveryEnabled() != null) listing.setDeliveryEnabled(cmd.deliveryEnabled());
        listing.setUpdatedAt(OffsetDateTime.now());
        listingRepo.save(listing);
    }

    private VehicleListingJpaEntity createDraftListing(UUID vehicleId) {
        VehicleListingJpaEntity listing = new VehicleListingJpaEntity();
        listing.setId(UUID.randomUUID());
        listing.setVehicleId(vehicleId);
        listing.setStatus(VehicleListingStatus.DRAFT);
        listing.setCurrency("VND");
        listing.setInstantBookingEnabled(false);
        listing.setDeliveryEnabled(false);
        OffsetDateTime now = OffsetDateTime.now();
        listing.setCreatedAt(now);
        listing.setUpdatedAt(now);
        return listing;
    }

    // Bulk replace: delete-all + insert-all in one transaction. Simpler than
    // diffing add/update/delete, and admin image counts are typically small.
    @Override
    public void replaceImages(SyncVehicleImagesCommand cmd) {
        imageRepo.deleteByVehicleId(cmd.vehicleId());
        imageRepo.flush();
        OffsetDateTime now = OffsetDateTime.now();
        for (SyncVehicleImagesCommand.ImageInput input : cmd.images()) {
            VehicleImageJpaEntity entity = new VehicleImageJpaEntity();
            entity.setId(UUID.randomUUID());
            entity.setVehicleId(cmd.vehicleId());
            entity.setFileUrl(input.fileUrl());
            entity.setSortOrder(input.sortOrder());
            entity.setIsCover(input.cover());
            entity.setCreatedAt(now);
            imageRepo.save(entity);
        }
    }

    @Override
    public void replaceFeatures(SyncVehicleFeaturesCommand cmd) {
        featureRepo.deleteByVehicleId(cmd.vehicleId());
        featureRepo.flush();
        OffsetDateTime now = OffsetDateTime.now();
        for (SyncVehicleFeaturesCommand.FeatureInput input : cmd.features()) {
            VehicleFeatureJpaEntity entity = new VehicleFeatureJpaEntity();
            entity.setId(UUID.randomUUID());
            entity.setVehicleId(cmd.vehicleId());
            entity.setCode(input.code());
            entity.setName(input.name());
            entity.setCreatedAt(now);
            featureRepo.save(entity);
        }
    }

    // pricing.price_plans lives in a different module — we touch it via native SQL
    // until the pricing module exposes an application port. The query uses a
    // single UPSERT pattern: deactivate existing ACTIVE plans for this vehicle,
    // then insert a fresh one.
    @Override
    public void upsertActivePricePlan(UpsertPricePlanCommand cmd) {
        OffsetDateTime now = OffsetDateTime.now();
        em.createNativeQuery(sql.load(VehicleSqlPaths.DEACTIVATE_ACTIVE_PRICE_PLANS))
                .setParameter("now", now)
                .setParameter("vid", cmd.vehicleId())
                .executeUpdate();

        em.createNativeQuery(sql.load(VehicleSqlPaths.INSERT_PRICE_PLAN))
                .setParameter("id", UUID.randomUUID())
                .setParameter("vid", cmd.vehicleId())
                .setParameter("name", cmd.name())
                .setParameter("currency", cmd.currency())
                .setParameter("baseDailyRate", cmd.baseDailyRate())
                .setParameter("hourlyRate", cmd.hourlyRate())
                .setParameter("weekendMultiplier",
                        cmd.weekendMultiplier() == null ? java.math.BigDecimal.ONE : cmd.weekendMultiplier())
                .setParameter("depositAmount",
                        cmd.depositAmount() == null ? java.math.BigDecimal.ZERO : cmd.depositAmount())
                .setParameter("now", now)
                .executeUpdate();
    }

    private static VehicleListing toDomain(VehicleListingJpaEntity e) {
        VehicleListingStatus status = e.getStatus() == null
                ? VehicleListingStatus.DRAFT
                : e.getStatus();
        return new VehicleListing(
                e.getId(),
                e.getVehicleId(),
                e.getTitle(),
                e.getCity(),
                e.getBaseDailyRate(),
                status,
                e.getPublishedAt()
        );
    }
}
