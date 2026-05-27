package com.ares.car_rental_monolith.modules.customer.application.view;

// Aggregate counts for the admin customer dashboard cards. Field names mirror
// the admin-web CustomerStatsSummary type.
public record CustomerStats(
        long total,
        long renters,
        long hosts,
        long pendingOrBlocked
) {}
