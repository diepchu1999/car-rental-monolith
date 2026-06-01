UPDATE customer.customers
SET full_name = :fullName,
    phone = CAST(:phone AS varchar),
    email = CAST(:email AS varchar),
    date_of_birth = CAST(:dob AS date),
    gender = CAST(:gender AS varchar),
    updated_at = :now
WHERE id = :id
