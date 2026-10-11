-- ============================================================
-- 03_views.sql — Database views
-- ============================================================

USE railway_db;

-- View 1: Full train route with station names and times
DROP VIEW IF EXISTS v_train_route;
CREATE VIEW v_train_route AS
SELECT
    t.Train_ID,
    t.Train_Number,
    t.Train_Name,
    t.Train_Type,
    ts.Stop_No,
    s.Station_ID,
    s.Station_Name,
    s.Station_Code,
    s.City,
    ts.Arrival_Time,
    ts.Departure_Time,
    ts.Distance_Km
FROM TRAIN t
JOIN TRAIN_STATION ts ON ts.Train_ID = t.Train_ID
JOIN STATION s ON s.Station_ID = ts.Station_ID
ORDER BY t.Train_ID, ts.Stop_No;

-- View 2: Booking summary with train and station names and payment status
DROP VIEW IF EXISTS v_booking_summary;
CREATE VIEW v_booking_summary AS
SELECT
    b.Booking_ID,
    b.PNR,
    b.Status AS Booking_Status,
    b.Journey_Date,
    b.Class_Type,
    b.Total_Fare,
    b.Booked_At,
    b.Contact_Email,
    t.Train_Number,
    t.Train_Name,
    ss.Station_Name AS Source_Station,
    ds.Station_Name AS Destination_Station,
    p.Payment_Status,
    p.Payment_Method,
    p.Amount AS Payment_Amount
FROM BOOKING b
JOIN TRAIN t ON t.Train_ID = b.Train_ID
JOIN STATION ss ON ss.Station_ID = b.Source_Station_ID
JOIN STATION ds ON ds.Station_ID = b.Destination_Station_ID
LEFT JOIN PAYMENT p ON p.Booking_ID = b.Booking_ID;

-- View 3: Base seat availability (all seats with coach/class/train info)
DROP VIEW IF EXISTS v_seat_availability_base;
CREATE VIEW v_seat_availability_base AS
SELECT
    t.Train_ID,
    t.Train_Number,
    t.Train_Name,
    c.Coach_ID,
    c.Coach_Number,
    c.Class_Type,
    s.Seat_ID,
    s.Seat_Number,
    s.Seat_Type
FROM TRAIN t
JOIN COACH c ON c.Train_ID = t.Train_ID
JOIN SEAT s ON s.Coach_ID = c.Coach_ID;
