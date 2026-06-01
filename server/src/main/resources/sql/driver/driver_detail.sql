SELECT id, driver_code, full_name, phone, license_number, license_class,
       license_expiry_date, years_of_experience, rating_average,
       rating_count, status
FROM driver.drivers
WHERE id = :id
