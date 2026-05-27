package com.ares.car_rental_monolith.modules.customer.adapter.out.persistence;

import com.ares.car_rental_monolith.modules.customer.adapter.out.persistence.entity.CustomerJpaEntity;
import com.ares.car_rental_monolith.modules.customer.adapter.out.persistence.repository.CustomerJpaRepository;
import com.ares.car_rental_monolith.modules.customer.application.port.out.WriteCustomerPort;
import com.ares.car_rental_monolith.modules.customer.application.view.CustomerDetail;
import jakarta.persistence.EntityManager;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Component;

// Native-SQL writes, đồng bộ phần còn lại của customer module. Insert chạy
// trong transaction của service gọi vào. Nullable params dùng CAST(:p AS ...)
// để driver bind SQL NULL không bị Hibernate infer type sai.
@Component
class CustomerWriteAdapter implements WriteCustomerPort {

    private final EntityManager em;
    private final CustomerJpaRepository customerJpaRepository;

    CustomerWriteAdapter(EntityManager em, CustomerJpaRepository customerJpaRepository) {
        this.em = em;
        this.customerJpaRepository = customerJpaRepository;
    }

    @Override
    public void create(CustomerDetail customer) {
        OffsetDateTime now = customer.joinedAt();

        insertCustomer(customer, now);
        insertRoles(customer, now);
        if (customer.hostProfile() != null) {
            insertHostProfile(customer.id(), customer.hostProfile(), now);
        }
        if (customer.kycs() != null) {
            for (CustomerDetail.Kyc kyc : customer.kycs()) {
                insertKyc(customer.id(), kyc, now);
            }
        }
        for (CustomerDetail.Address address : customer.addresses()) {
            insertAddress(customer.id(), address, now);
        }
    }

    @Override
    public void saveCustomerStatus(CustomerDetail customer) {
        CustomerJpaEntity entity = customerJpaRepository.findById(customer.id())
                .orElseThrow(() -> new IllegalStateException(
                        "Customer vanished during update: " + customer.id()));
        entity.setStatus(customer.status());
        entity.setUpdatedAt(OffsetDateTime.now());
        customerJpaRepository.save(entity);
    }

    @Override
    public void saveHostStatus(CustomerDetail customer) {
        OffsetDateTime now = OffsetDateTime.now();
        int updated = em.createNativeQuery("""
                        UPDATE customer.host_profiles
                        SET status = :status, updated_at = :now
                        WHERE customer_id = :cid
                        """)
                .setParameter("status", customer.hostProfile().status())
                .setParameter("now", now)
                .setParameter("cid", customer.id())
                .executeUpdate();
        if (updated == 0) {
            throw new IllegalStateException(
                    "Host profile vanished during update: " + customer.id());
        }
    }

    @Override
    public void saveCustomerBasics(CustomerDetail customer) {
        OffsetDateTime now = OffsetDateTime.now();
        // UPDATE thẳng — không load entity rồi setter rồi save, tránh 1 round
        // trip thừa và không kéo state ngoài columns mình thực sự đổi.
        int updated = em.createNativeQuery("""
                        UPDATE customer.customers
                        SET full_name = :fullName,
                            phone = CAST(:phone AS varchar),
                            email = CAST(:email AS varchar),
                            date_of_birth = CAST(:dob AS date),
                            gender = CAST(:gender AS varchar),
                            updated_at = :now
                        WHERE id = :id
                        """)
                .setParameter("fullName", customer.fullName())
                .setParameter("phone", customer.phone())
                .setParameter("email", customer.email())
                .setParameter("dob", customer.dateOfBirth())
                .setParameter("gender", customer.gender())
                .setParameter("now", now)
                .setParameter("id", customer.id())
                .executeUpdate();
        if (updated == 0) {
            throw new IllegalStateException(
                    "Customer vanished during update: " + customer.id());
        }
    }

    @Override
    public int approveKyc(UUID kycId, UUID reviewedBy, OffsetDateTime now) {
        return em.createNativeQuery("""
                        UPDATE customer.kyc_profiles
                        SET status = 'APPROVED',
                            reviewed_by = :reviewedBy,
                            reviewed_at = :now,
                            rejection_reason = NULL,
                            updated_at = :now
                        WHERE id = :kycId
                        """)
                .setParameter("reviewedBy", reviewedBy)
                .setParameter("now", now)
                .setParameter("kycId", kycId)
                .executeUpdate();
    }

    @Override
    public void createKyc(UUID customerId, CustomerDetail.Kyc kyc) {
        OffsetDateTime now = kyc.submittedAt() != null ? kyc.submittedAt() : OffsetDateTime.now();
        insertKyc(customerId, kyc, now);
        if (kyc.documents() != null) {
            for (CustomerDetail.Kyc.Document doc : kyc.documents()) {
                insertKycDocument(kyc.id(), doc, now);
            }
        }
    }

    private void insertKycDocument(UUID kycId, CustomerDetail.Kyc.Document doc, OffsetDateTime now) {
        UUID id = doc.id() != null ? doc.id() : UUID.randomUUID();
        em.createNativeQuery("""
                        INSERT INTO customer.kyc_documents (
                            id, kyc_profile_id, document_side, file_url, created_at
                        ) VALUES (
                            :id, :kycId, :side, :fileUrl, :now
                        )
                        """)
                .setParameter("id", id)
                .setParameter("kycId", kycId)
                .setParameter("side", doc.documentSide())
                .setParameter("fileUrl", doc.fileUrl())
                .setParameter("now", now)
                .executeUpdate();
    }

    @Override
    public int rejectKyc(UUID kycId, UUID reviewedBy, String rejectionReason, OffsetDateTime now) {
        return em.createNativeQuery("""
                        UPDATE customer.kyc_profiles
                        SET status = 'REJECTED',
                            reviewed_by = :reviewedBy,
                            reviewed_at = :now,
                            rejection_reason = :reason,
                            updated_at = :now
                        WHERE id = :kycId
                        """)
                .setParameter("reviewedBy", reviewedBy)
                .setParameter("now", now)
                .setParameter("reason", rejectionReason)
                .setParameter("kycId", kycId)
                .executeUpdate();
    }

    private void insertCustomer(CustomerDetail c, OffsetDateTime now) {
        em.createNativeQuery("""
                        INSERT INTO customer.customers (
                            id, user_id, full_name, phone, email, date_of_birth, gender,
                            status, created_at, updated_at
                        ) VALUES (
                            :id, :userId, :fullName, CAST(:phone AS varchar), CAST(:email AS varchar),
                            CAST(:dob AS date), CAST(:gender AS varchar), :status, :now, :now
                        )
                        """)
                .setParameter("id", c.id())
                .setParameter("userId", UUID.randomUUID())
                .setParameter("fullName", c.fullName())
                .setParameter("phone", c.phone())
                .setParameter("email", c.email())
                .setParameter("dob", c.dateOfBirth())
                .setParameter("gender", c.gender())
                .setParameter("status", c.status())
                .setParameter("now", now)
                .executeUpdate();
    }

    private void insertRoles(CustomerDetail c, OffsetDateTime now) {
        for (String role : c.roles()) {
            em.createNativeQuery("""
                            INSERT INTO customer.customer_roles (customer_id, role, created_at)
                            VALUES (:cid, :role, :now)
                            """)
                    .setParameter("cid", c.id())
                    .setParameter("role", role)
                    .setParameter("now", now)
                    .executeUpdate();
        }
    }

    private void insertHostProfile(UUID customerId, CustomerDetail.HostProfile host, OffsetDateTime now) {
        em.createNativeQuery("""
                        INSERT INTO customer.host_profiles (
                            id, customer_id, host_code, display_name, bio,
                            rating_average, rating_count, status, created_at, updated_at
                        ) VALUES (
                            :id, :cid, :hostCode, :displayName, CAST(:bio AS text),
                            :ratingAverage, :ratingCount, :status, :now, :now
                        )
                        """)
                .setParameter("id", UUID.randomUUID())
                .setParameter("cid", customerId)
                .setParameter("hostCode", host.hostCode())
                .setParameter("displayName", host.displayName())
                .setParameter("bio", host.bio())
                .setParameter("ratingAverage", host.ratingAverage())
                .setParameter("ratingCount", host.ratingCount())
                .setParameter("status", host.status())
                .setParameter("now", now)
                .executeUpdate();
    }

    private void insertKyc(UUID customerId, CustomerDetail.Kyc kyc, OffsetDateTime now) {
        UUID kycId = kyc.id() != null ? kyc.id() : UUID.randomUUID();
        String kycCode = kyc.kycCode() != null
                ? kyc.kycCode()
                : "KYC-" + kycId.toString().substring(0, 8).toUpperCase();
        em.createNativeQuery("""
                        INSERT INTO customer.kyc_profiles (
                            id, customer_id, kyc_code, legal_name, document_type, document_number,
                            issued_date, issued_place, status, created_at, updated_at
                        ) VALUES (
                            :id, :cid, :kycCode, :legalName, :documentType, :documentNumber,
                            CAST(:issuedDate AS date), CAST(:issuedPlace AS varchar), :status, :now, :now
                        )
                        """)
                .setParameter("id", kycId)
                .setParameter("cid", customerId)
                .setParameter("kycCode", kycCode)
                .setParameter("legalName", kyc.legalName())
                .setParameter("documentType", kyc.documentType())
                .setParameter("documentNumber", kyc.documentNumber())
                .setParameter("issuedDate", kyc.issuedDate())
                .setParameter("issuedPlace", kyc.issuedPlace())
                .setParameter("status", kyc.status())
                .setParameter("now", now)
                .executeUpdate();
    }

    private void insertAddress(UUID customerId, CustomerDetail.Address address, OffsetDateTime now) {
        em.createNativeQuery("""
                        INSERT INTO customer.addresses (
                            id, customer_id, label, line1, ward, district, city, country,
                            province_code, commune_code, is_default, created_at
                        ) VALUES (
                            :id, :cid, CAST(:label AS varchar), :line1, CAST(:ward AS varchar),
                            CAST(:district AS varchar), :city, :country,
                            CAST(:provinceCode AS varchar), CAST(:communeCode AS varchar), :isDefault, :now
                        )
                        """)
                .setParameter("id", address.id())
                .setParameter("cid", customerId)
                .setParameter("label", address.label())
                .setParameter("line1", address.line1())
                .setParameter("ward", address.communeName())
                .setParameter("district", address.legacyDistrict())
                .setParameter("city", address.provinceName())
                .setParameter("country", "Vietnam")
                .setParameter("provinceCode", address.provinceCode())
                .setParameter("communeCode", address.communeCode())
                .setParameter("isDefault", address.isDefault())
                .setParameter("now", now)
                .executeUpdate();
    }
}
