UPDATE customer.kyc_profiles
SET status = 'REJECTED',
    reviewed_by = :reviewedBy,
    reviewed_at = :now,
    rejection_reason = :reason,
    updated_at = :now
WHERE id = :kycId
