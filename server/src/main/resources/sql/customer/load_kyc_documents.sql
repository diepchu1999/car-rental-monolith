SELECT id, kyc_profile_id, document_side, file_url, created_at
FROM customer.kyc_documents
WHERE kyc_profile_id IN (:kycIds)
ORDER BY
    CASE document_side
        WHEN 'FRONT' THEN 0 WHEN 'BACK' THEN 1
        WHEN 'SELFIE' THEN 2 ELSE 3 END,
    created_at ASC
