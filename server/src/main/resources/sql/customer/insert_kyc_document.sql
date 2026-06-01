INSERT INTO customer.kyc_documents (
    id, kyc_profile_id, document_side, file_url, created_at
) VALUES (
    :id, :kycId, :side, :fileUrl, :now
)
