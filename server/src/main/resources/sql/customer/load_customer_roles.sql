SELECT role FROM customer.customer_roles
WHERE customer_id = :id
ORDER BY role
