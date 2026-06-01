INSERT INTO customer.customers (
    id, user_id, full_name, phone, email, date_of_birth, gender,
    status, created_at, updated_at
) VALUES (
    :id, :userId, :fullName, CAST(:phone AS varchar), CAST(:email AS varchar),
    CAST(:dob AS date), CAST(:gender AS varchar), :status, :now, :now
)
