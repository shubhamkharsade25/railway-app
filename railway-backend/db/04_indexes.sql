-- ============================================================
-- 04_indexes.sql — Performance indexes
-- Run AFTER 02_seed.sql
-- Note: MySQL 8 does not support CREATE INDEX IF NOT EXISTS.
-- Run this script only once after a fresh schema.
-- ============================================================

USE railway_db;

-- Route search: find trains stopping at a station
CREATE INDEX idx_ts_station ON TRAIN_STATION (Station_ID, Train_ID);

-- Booking queries by train/date/status
CREATE INDEX idx_booking_journey ON BOOKING (Train_ID, Journey_Date, Status);

-- Booking queries by booking time (for reports)
CREATE INDEX idx_booking_booked_at ON BOOKING (Booked_At);

-- Payment lookup by booking
CREATE INDEX idx_payment_booking ON PAYMENT (Booking_ID, Payment_Status);

-- Seat reservation lookup by seat + date (used in availability check)
CREATE INDEX idx_sr_seat_date ON SEAT_RESERVATION (Seat_ID, Journey_Date, Status);
