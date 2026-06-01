UPDATE customer.kyc_profiles
SET status = 'APPROVED',
    reviewed_by = :reviewedBy,
    reviewed_at = :now,
    rejection_reason = NULL,
    updated_at = :now
WHERE id = :kycId
