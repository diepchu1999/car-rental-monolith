INSERT INTO customer.kyc_profiles (
    id, customer_id, kyc_code, legal_name, document_type, document_number,
    issued_date, issued_place, status, created_at, updated_at
) VALUES (
    :id, :cid, :kycCode, :legalName, :documentType, :documentNumber,
    CAST(:issuedDate AS date), CAST(:issuedPlace AS varchar), :status, :now, :now
)
