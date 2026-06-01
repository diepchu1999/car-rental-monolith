SELECT id, kyc_code, legal_name, document_type, document_number,
       issued_date, issued_place, status, reviewed_by, reviewed_at,
       rejection_reason, created_at
FROM customer.kyc_profiles
WHERE id = :kycId AND customer_id = :customerId
