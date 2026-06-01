CASE
    WHEN ka.total = 0 THEN 'NO_KYC'
    WHEN ka.approved = ka.total THEN 'FULLY_APPROVED'
    WHEN ka.rejected = ka.total THEN 'REJECTED'
    WHEN ka.approved > 0 THEN 'PARTIALLY_APPROVED'
    ELSE 'PENDING'
END
