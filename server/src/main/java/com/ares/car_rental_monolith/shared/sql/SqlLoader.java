package com.ares.car_rental_monolith.shared.sql;

/**
 * Load nội dung 1 file SQL từ resource path. Trách nhiệm DUY NHẤT: trả về SQL
 * text. KHÔNG execute, KHÔNG bind parameter, KHÔNG map kết quả — những việc đó
 * vẫn thuộc về persistence adapter.
 *
 * <p>Adapter phụ thuộc vào abstraction này, không phụ thuộc implementation cụ
 * thể, để dễ thay nguồn SQL (classpath, cache khác, test stub) mà không sửa
 * adapter (DIP).
 */
public interface SqlLoader {

    /**
     * @param resourcePath đường dẫn classpath tới file SQL,
     *                     vd {@code "sql/customer/insert_customer.sql"}.
     * @return nội dung file SQL.
     * @throws IllegalArgumentException nếu resource không tồn tại trên classpath.
     */
    String load(String resourcePath);
}
