SELECT id, full_name, phone, email, date_of_birth, gender, status, created_at
FROM customer.customers
WHERE id = :id
