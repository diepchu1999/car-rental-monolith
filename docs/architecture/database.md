# Database Structure

The backend uses PostgreSQL with schema-per-module. Each business module owns
its tables and should not create foreign keys to another business schema.
Cross-module references are stored as UUID values and immutable snapshots.

## Schemas

- `common`: outbox events and idempotency keys.
- `identity`: users, roles, permissions, sessions.
- `customer`: renter/host profile, KYC, addresses.
- `vehicle`: marketplace vehicle data, listings, images, availability blocks.
- `fleet`: company-owned vehicle asset data, maintenance, insurance, branches.
- `driver`: company driver profile, documents, availability, assignments.
- `pricing`: price plans, promotions, booking quotes.
- `booking`: booking lifecycle, status history, pickup/return checklists.
- `payment`: payment intents, transactions, refunds, payouts.
- `review`: reviews, replies, reports.
- `notification`: templates, notifications, delivery logs, devices.
- `admin`: audit logs, backoffice tasks, dashboard snapshots.

## Key Rules

- `booking.bookings` is the operational center.
- `booking.bookings.vehicle_id`, `customer_id`, `driver_id`, and `quote_id`
  are UUID references only, with no cross-schema foreign key.
- Booking stores `vehicle_snapshot`, `customer_snapshot`, and
  `driver_snapshot` so old bookings remain stable after profile changes.
- `vehicle.vehicles.source` separates `HOST_OWNED` and `COMPANY_OWNED`.
- `booking.bookings.service_type` separates `SELF_DRIVE` and `WITH_DRIVER`.
- `fleet.company_vehicles.vehicle_id` links to the public vehicle listing by
  UUID only, keeping Fleet and Vehicle separable later.
- Use `common.outbox_events` for important module events before moving to
  Kafka or RabbitMQ.

## Main Booking Variants

| service_type | vehicle_source | Meaning |
| --- | --- | --- |
| `SELF_DRIVE` | `HOST_OWNED` | P2P self-drive rental |
| `SELF_DRIVE` | `COMPANY_OWNED` | Company car self-drive rental |
| `WITH_DRIVER` | `COMPANY_OWNED` | Company car with company driver |
| `WITH_DRIVER` | `HOST_OWNED` | Future extension if needed |
