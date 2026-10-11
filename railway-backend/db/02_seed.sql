-- ============================================================
-- 02_seed.sql — SAMPLE DATA for college project.
-- Not official railway data. All train names, numbers, times, and fares are fictional.
-- Re-runnable: deletes data in reverse FK order first, then re-inserts.
-- ============================================================

USE railway_db;

-- Clear in reverse dependency order
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM CANCELLATION;
DELETE FROM PAYMENT;
DELETE FROM SEAT_RESERVATION;
DELETE FROM BOOKING_PASSENGER;
DELETE FROM BOOKING;
DELETE FROM PASSENGER;
DELETE FROM SEAT;
DELETE FROM COACH;
DELETE FROM TRAIN_STATION;
DELETE FROM CLASS_FARE;
DELETE FROM TRAIN;
DELETE FROM STATION;
SET FOREIGN_KEY_CHECKS = 1;

-- Reset AUTO_INCREMENT counters
ALTER TABLE STATION         AUTO_INCREMENT = 1;
ALTER TABLE TRAIN           AUTO_INCREMENT = 1;
ALTER TABLE COACH           AUTO_INCREMENT = 1;
ALTER TABLE SEAT            AUTO_INCREMENT = 1;
ALTER TABLE PASSENGER       AUTO_INCREMENT = 1;
ALTER TABLE BOOKING         AUTO_INCREMENT = 1;
ALTER TABLE SEAT_RESERVATION AUTO_INCREMENT = 1;
ALTER TABLE PAYMENT         AUTO_INCREMENT = 1;
ALTER TABLE CANCELLATION    AUTO_INCREMENT = 1;

-- ============================================================
-- STATIONS (12)
-- ============================================================
INSERT INTO STATION (Station_Name, Station_Code, City) VALUES
('Mumbai Central',  'MMCT', 'Mumbai'),
('Borivali',        'BVI',  'Mumbai'),
('Vapi',            'VAPI', 'Vapi'),
('Surat',           'ST',   'Surat'),
('Bharuch',         'BH',   'Bharuch'),
('Vadodara',        'BRC',  'Vadodara'),
('Anand',           'ANND', 'Anand'),
('Ahmedabad',       'ADI',  'Ahmedabad'),
('Pune',            'PUNE', 'Pune'),
('Lonavala',        'LNL',  'Lonavala'),
('Nashik Road',     'NK',   'Nashik'),
('Delhi',           'NDLS', 'New Delhi');

-- IDs assigned: MMCT=1, BVI=2, VAPI=3, ST=4, BH=5, BRC=6, ANND=7, ADI=8, PUNE=9, LNL=10, NK=11, NDLS=12

-- ============================================================
-- TRAINS (6)
-- SAMPLE data — not official Indian Railways data.
-- ============================================================
INSERT INTO TRAIN (Train_Number, Train_Name, Train_Type, Is_Active) VALUES
('12901', 'Gujarat Express (Sample)',     'EXPRESS',   1),
('12933', 'Karnavati Express (Sample)',   'EXPRESS',   1),
('12009', 'Shatabdi Express (Sample)',    'SUPERFAST', 1),
('19411', 'Konkan Express (Sample)',      'EXPRESS',   1),
('12261', 'Duronto Express (Sample)',     'SUPERFAST', 1),
('59507', 'Mumbai Local (Sample)',        'LOCAL',     1);

-- IDs: Gujarat=1, Karnavati=2, Shatabdi=3, Konkan=4, Duronto=5, Local=6

-- ============================================================
-- ROUTES (TRAIN_STATION)
-- ============================================================

-- Train 1: Gujarat Express (Sample)  MMCT -> BVI -> VAPI -> ST -> BH -> BRC -> ADI
INSERT INTO TRAIN_STATION (Train_ID, Station_ID, Stop_No, Arrival_Time, Departure_Time, Distance_Km) VALUES
(1, 1, 1, NULL,      '07:00', 0),    -- Mumbai Central (origin)
(1, 2, 2, '07:25', '07:27', 28),   -- Borivali
(1, 3, 3, '09:15', '09:17', 158),  -- Vapi
(1, 4, 4, '10:45', '10:50', 263),  -- Surat
(1, 5, 5, '11:55', '11:57', 349),  -- Bharuch
(1, 6, 6, '12:45', '12:50', 392),  -- Vadodara
(1, 8, 7, '14:30', NULL,    492);  -- Ahmedabad (terminus)

-- Train 2: Karnavati Express (Sample)  MMCT -> VAPI -> ST -> BRC -> ANND -> ADI
INSERT INTO TRAIN_STATION (Train_ID, Station_ID, Stop_No, Arrival_Time, Departure_Time, Distance_Km) VALUES
(2, 1, 1, NULL,      '15:00', 0),
(2, 3, 2, '17:10', '17:12', 158),
(2, 4, 3, '18:30', '18:35', 263),
(2, 6, 4, '20:10', '20:15', 392),
(2, 7, 5, '20:55', '20:57', 432),
(2, 8, 6, '21:45', NULL,    492);

-- Train 3: Shatabdi Express (Sample)  MMCT -> BVI -> LNL -> PUNE
-- (Mumbai to Pune route — shares BVI with Gujarat Express, different direction)
INSERT INTO TRAIN_STATION (Train_ID, Station_ID, Stop_No, Arrival_Time, Departure_Time, Distance_Km) VALUES
(3, 1, 1, NULL,      '06:00', 0),
(3, 2, 2, '06:30', '06:32', 28),
(3, 10, 3, '07:55', '07:58', 96),   -- Lonavala
(3, 9, 4, '09:00', NULL,    192);   -- Pune

-- Train 4: Konkan Express (Sample)  PUNE -> NK -> NDLS
-- Pune to Delhi, does NOT visit Mumbai Central
INSERT INTO TRAIN_STATION (Train_ID, Station_ID, Stop_No, Arrival_Time, Departure_Time, Distance_Km) VALUES
(4, 9,  1, NULL,      '20:00', 0),
(4, 11, 2, '23:30', '23:35', 180),  -- Nashik Road
(4, 12, 3, '14:30', NULL,   1475); -- Delhi (next day)

-- Train 5: Duronto Express (Sample)  MMCT -> ADI (non-stop)
INSERT INTO TRAIN_STATION (Train_ID, Station_ID, Stop_No, Arrival_Time, Departure_Time, Distance_Km) VALUES
(5, 1, 1, NULL,      '16:00', 0),
(5, 8, 2, '21:00', NULL,    492);

-- Train 6: Mumbai Local (Sample)  MMCT -> BVI (local)
INSERT INTO TRAIN_STATION (Train_ID, Station_ID, Stop_No, Arrival_Time, Departure_Time, Distance_Km) VALUES
(6, 1, 1, NULL,      '08:00', 0),
(6, 2, 2, '08:45', NULL,    28);

-- ============================================================
-- COACHES
-- For each express train: 2 SL coaches (S1,S2), 1 3A (B1), 1 2A (A1)
-- Shatabdi: 1 CC (C1), 1 2A (A1)
-- Local: 1 SL only
-- ============================================================

-- Train 1 (Gujarat Express)
INSERT INTO COACH (Train_ID, Coach_Number, Class_Type) VALUES
(1,'S1','SL'), (1,'S2','SL'), (1,'B1','3A'), (1,'A1','2A');

-- Train 2 (Karnavati Express)
INSERT INTO COACH (Train_ID, Coach_Number, Class_Type) VALUES
(2,'S1','SL'), (2,'S2','SL'), (2,'B1','3A'), (2,'A1','2A');

-- Train 3 (Shatabdi)
INSERT INTO COACH (Train_ID, Coach_Number, Class_Type) VALUES
(3,'C1','CC'), (3,'A1','2A');

-- Train 4 (Konkan Express)
INSERT INTO COACH (Train_ID, Coach_Number, Class_Type) VALUES
(4,'S1','SL'), (4,'S2','SL'), (4,'B1','3A'), (4,'A1','2A'), (4,'H1','1A');

-- Train 5 (Duronto Express)
INSERT INTO COACH (Train_ID, Coach_Number, Class_Type) VALUES
(5,'S1','SL'), (5,'S2','SL'), (5,'B1','3A'), (5,'A1','2A');

-- Train 6 (Local)
INSERT INTO COACH (Train_ID, Coach_Number, Class_Type) VALUES
(6,'S1','SL');

-- ============================================================
-- SEATS (generated via procedure for correct counts)
-- SL: 72 seats, 3A: 64, 2A: 48, 1A: 24, CC: 52
-- Seat type pattern (seat_no % 8):
--  1=LOWER, 2=MIDDLE, 3=UPPER, 4=SIDE_LOWER,
--  5=LOWER, 6=MIDDLE, 7=UPPER, 0=SIDE_UPPER
-- ============================================================

DROP PROCEDURE IF EXISTS generate_seats;

DELIMITER //
CREATE PROCEDURE generate_seats(
    IN p_coach_id INT,
    IN p_seat_count INT
)
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE seat_type VARCHAR(20);
    WHILE i <= p_seat_count DO
        CASE (i % 8)
            WHEN 1 THEN SET seat_type = 'LOWER';
            WHEN 2 THEN SET seat_type = 'MIDDLE';
            WHEN 3 THEN SET seat_type = 'UPPER';
            WHEN 4 THEN SET seat_type = 'SIDE_LOWER';
            WHEN 5 THEN SET seat_type = 'LOWER';
            WHEN 6 THEN SET seat_type = 'MIDDLE';
            WHEN 7 THEN SET seat_type = 'UPPER';
            ELSE        SET seat_type = 'SIDE_UPPER';
        END CASE;
        INSERT INTO SEAT (Coach_ID, Seat_Number, Seat_Type) VALUES (p_coach_id, i, seat_type);
        SET i = i + 1;
    END WHILE;
END //
DELIMITER ;

-- Generate seats for each coach
-- Train 1 (Gujarat): coaches 1,2=SL(72), 3=3A(64), 4=2A(48)
CALL generate_seats(1, 72);
CALL generate_seats(2, 72);
CALL generate_seats(3, 64);
CALL generate_seats(4, 48);

-- Train 2 (Karnavati): coaches 5,6=SL(72), 7=3A(64), 8=2A(48)
CALL generate_seats(5, 72);
CALL generate_seats(6, 72);
CALL generate_seats(7, 64);
CALL generate_seats(8, 48);

-- Train 3 (Shatabdi): coach 9=CC(52), 10=2A(48)
CALL generate_seats(9, 52);
CALL generate_seats(10, 48);

-- Train 4 (Konkan): coaches 11,12=SL(72), 13=3A(64), 14=2A(48), 15=1A(24)
CALL generate_seats(11, 72);
CALL generate_seats(12, 72);
CALL generate_seats(13, 64);
CALL generate_seats(14, 48);
CALL generate_seats(15, 24);

-- Train 5 (Duronto): coaches 16,17=SL(72), 18=3A(64), 19=2A(48)
CALL generate_seats(16, 72);
CALL generate_seats(17, 72);
CALL generate_seats(18, 64);
CALL generate_seats(19, 48);

-- Train 6 (Local): coach 20=SL(72)
CALL generate_seats(20, 72);

DROP PROCEDURE IF EXISTS generate_seats;

-- ============================================================
-- CLASS_FARE (SAMPLE rates — not official)
-- ============================================================
INSERT INTO CLASS_FARE (Class_Type, Rate_Per_Km, Base_Fare) VALUES
('SL',  0.60, 40.00),
('3A',  1.60, 100.00),
('2A',  2.40, 150.00),
('1A',  4.00, 250.00),
('CC',  1.40, 90.00);

-- ============================================================
-- HISTORICAL BOOKINGS (sample, for reports testing)
-- ~15 bookings over last 30 days
-- Using Train 1 (Gujarat Express, ID=1) between MMCT(1) and ADI(8)
-- and Train 2 (Karnavati, ID=2) between MMCT(1) and ADI(8)
-- ============================================================

-- Note: Seat IDs for Train 1, SL coach S1 (coach_id=1): seats 1-72
-- Seat IDs for Train 1, 3A coach B1 (coach_id=3): seats 145-208

-- Passengers
INSERT INTO PASSENGER (Passenger_Name, Age, Gender) VALUES
('Ramesh Sharma',   35, 'M'),
('Priya Nair',      28, 'F'),
('Anil Gupta',      45, 'M'),
('Sunita Patel',    30, 'F'),
('Vikram Singh',    22, 'M'),
('Meena Joshi',     40, 'F'),
('Rohit Kumar',     29, 'M'),
('Kavya Reddy',     25, 'F'),
('Suresh Iyer',     55, 'M'),
('Divya Shah',      33, 'F'),
('Raju Mishra',     38, 'M'),
('Lakshmi Pillai',  42, 'F'),
('Mohan Das',       60, 'M'),
('Anita Bose',      27, 'F'),
('Kiran Rao',       31, 'M'),
('Deepa Menon',     36, 'F'),
('Sanjay Tiwari',   48, 'M'),
('Nisha Agarwal',   24, 'F');

-- Booking 1: confirmed, SUCCESS
INSERT INTO BOOKING (PNR, Train_ID, Source_Station_ID, Destination_Station_ID, Class_Type, Journey_Date, Status, Total_Fare, Contact_Email, Contact_Phone, Booked_At)
VALUES ('1000000001', 1, 1, 8, 'SL', DATE_SUB(CURDATE(), INTERVAL 25 DAY), 'CONFIRMED', 595.20, 'ramesh@example.com', '9111111111', DATE_SUB(NOW(), INTERVAL 27 DAY));
INSERT INTO BOOKING_PASSENGER VALUES (1, 1);
INSERT INTO SEAT_RESERVATION (Booking_ID, Passenger_ID, Seat_ID, Journey_Date, Status, Active_Flag) VALUES (1, 1, 1, DATE_SUB(CURDATE(), INTERVAL 25 DAY), 'ACTIVE', 1);
INSERT INTO PAYMENT (Booking_ID, Amount, Payment_Method, Payment_Status, Paid_At) VALUES (1, 595.20, 'UPI', 'SUCCESS', DATE_SUB(NOW(), INTERVAL 27 DAY));

-- Booking 2: confirmed, SUCCESS, 2 passengers
INSERT INTO BOOKING (PNR, Train_ID, Source_Station_ID, Destination_Station_ID, Class_Type, Journey_Date, Status, Total_Fare, Contact_Email, Contact_Phone, Booked_At)
VALUES ('1000000002', 1, 1, 8, 'SL', DATE_SUB(CURDATE(), INTERVAL 20 DAY), 'CONFIRMED', 1190.40, 'priya@example.com', '9222222222', DATE_SUB(NOW(), INTERVAL 22 DAY));
INSERT INTO BOOKING_PASSENGER VALUES (2, 2), (2, 3);
INSERT INTO SEAT_RESERVATION (Booking_ID, Passenger_ID, Seat_ID, Journey_Date, Status, Active_Flag) VALUES
(2, 2, 2, DATE_SUB(CURDATE(), INTERVAL 20 DAY), 'ACTIVE', 1),
(2, 3, 3, DATE_SUB(CURDATE(), INTERVAL 20 DAY), 'ACTIVE', 1);
INSERT INTO PAYMENT (Booking_ID, Amount, Payment_Method, Payment_Status, Paid_At) VALUES (2, 1190.40, 'CARD', 'SUCCESS', DATE_SUB(NOW(), INTERVAL 22 DAY));

-- Booking 3: confirmed, 3A class
INSERT INTO BOOKING (PNR, Train_ID, Source_Station_ID, Destination_Station_ID, Class_Type, Journey_Date, Status, Total_Fare, Contact_Email, Contact_Phone, Booked_At)
VALUES ('1000000003', 1, 1, 8, '3A', DATE_SUB(CURDATE(), INTERVAL 18 DAY), 'CONFIRMED', 887.20, 'anil@example.com', '9333333333', DATE_SUB(NOW(), INTERVAL 20 DAY));
INSERT INTO BOOKING_PASSENGER VALUES (3, 4);
INSERT INTO SEAT_RESERVATION (Booking_ID, Passenger_ID, Seat_ID, Journey_Date, Status, Active_Flag) VALUES (3, 4, 145, DATE_SUB(CURDATE(), INTERVAL 18 DAY), 'ACTIVE', 1);
INSERT INTO PAYMENT (Booking_ID, Amount, Payment_Method, Payment_Status, Paid_At) VALUES (3, 887.20, 'NETBANKING', 'SUCCESS', DATE_SUB(NOW(), INTERVAL 20 DAY));

-- Booking 4: FAILED payment
INSERT INTO BOOKING (PNR, Train_ID, Source_Station_ID, Destination_Station_ID, Class_Type, Journey_Date, Status, Total_Fare, Contact_Email, Contact_Phone, Booked_At)
VALUES ('1000000004', 2, 1, 8, 'SL', DATE_SUB(CURDATE(), INTERVAL 15 DAY), 'FAILED', 595.20, 'vikram@example.com', '9444444444', DATE_SUB(NOW(), INTERVAL 16 DAY));
INSERT INTO BOOKING_PASSENGER VALUES (4, 5);
INSERT INTO PAYMENT (Booking_ID, Amount, Payment_Method, Payment_Status, Paid_At) VALUES (4, 595.20, 'UPI', 'FAILED', DATE_SUB(NOW(), INTERVAL 16 DAY));

-- Booking 5: confirmed
INSERT INTO BOOKING (PNR, Train_ID, Source_Station_ID, Destination_Station_ID, Class_Type, Journey_Date, Status, Total_Fare, Contact_Email, Contact_Phone, Booked_At)
VALUES ('1000000005', 2, 1, 8, '3A', DATE_SUB(CURDATE(), INTERVAL 12 DAY), 'CONFIRMED', 887.20, 'meena@example.com', '9555555555', DATE_SUB(NOW(), INTERVAL 14 DAY));
INSERT INTO BOOKING_PASSENGER VALUES (5, 6);
INSERT INTO SEAT_RESERVATION (Booking_ID, Passenger_ID, Seat_ID, Journey_Date, Status, Active_Flag) VALUES (5, 6, 209, DATE_SUB(CURDATE(), INTERVAL 12 DAY), 'ACTIVE', 1);
INSERT INTO PAYMENT (Booking_ID, Amount, Payment_Method, Payment_Status, Paid_At) VALUES (5, 887.20, 'CARD', 'SUCCESS', DATE_SUB(NOW(), INTERVAL 14 DAY));

-- Booking 6: CANCELLED
INSERT INTO BOOKING (PNR, Train_ID, Source_Station_ID, Destination_Station_ID, Class_Type, Journey_Date, Status, Total_Fare, Contact_Email, Contact_Phone, Booked_At)
VALUES ('1000000006', 1, 1, 6, 'SL', DATE_SUB(CURDATE(), INTERVAL 10 DAY), 'CANCELLED', 295.20, 'rohit@example.com', '9666666666', DATE_SUB(NOW(), INTERVAL 12 DAY));
INSERT INTO BOOKING_PASSENGER VALUES (6, 7);
INSERT INTO SEAT_RESERVATION (Booking_ID, Passenger_ID, Seat_ID, Journey_Date, Status, Active_Flag) VALUES (6, 7, 4, DATE_SUB(CURDATE(), INTERVAL 10 DAY), 'CANCELLED', NULL);
INSERT INTO PAYMENT (Booking_ID, Amount, Payment_Method, Payment_Status, Paid_At) VALUES (6, 295.20, 'UPI', 'REFUNDED', DATE_SUB(NOW(), INTERVAL 10 DAY));
INSERT INTO CANCELLATION (Booking_ID, Cancelled_At, Reason, Refund_Amount) VALUES (6, DATE_SUB(NOW(), INTERVAL 10 DAY), 'Change of plans', 236.16);

-- Booking 7: confirmed, future journey
INSERT INTO BOOKING (PNR, Train_ID, Source_Station_ID, Destination_Station_ID, Class_Type, Journey_Date, Status, Total_Fare, Contact_Email, Contact_Phone, Booked_At)
VALUES ('1000000007', 1, 1, 8, '2A', DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'CONFIRMED', 1330.80, 'kavya@example.com', '9777777777', DATE_SUB(NOW(), INTERVAL 2 DAY));
INSERT INTO BOOKING_PASSENGER VALUES (7, 8);
INSERT INTO SEAT_RESERVATION (Booking_ID, Passenger_ID, Seat_ID, Journey_Date, Status, Active_Flag) VALUES (7, 8, 273, DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'ACTIVE', 1);
INSERT INTO PAYMENT (Booking_ID, Amount, Payment_Method, Payment_Status, Paid_At) VALUES (7, 1330.80, 'CARD', 'SUCCESS', DATE_SUB(NOW(), INTERVAL 2 DAY));

-- Booking 8: confirmed
INSERT INTO BOOKING (PNR, Train_ID, Source_Station_ID, Destination_Station_ID, Class_Type, Journey_Date, Status, Total_Fare, Contact_Email, Contact_Phone, Booked_At)
VALUES ('1000000008', 2, 1, 8, 'SL', DATE_SUB(CURDATE(), INTERVAL 8 DAY), 'CONFIRMED', 595.20, 'suresh@example.com', '9888888888', DATE_SUB(NOW(), INTERVAL 9 DAY));
INSERT INTO BOOKING_PASSENGER VALUES (8, 9);
INSERT INTO SEAT_RESERVATION (Booking_ID, Passenger_ID, Seat_ID, Journey_Date, Status, Active_Flag) VALUES (8, 9, 5, DATE_SUB(CURDATE(), INTERVAL 8 DAY), 'ACTIVE', 1);
INSERT INTO PAYMENT (Booking_ID, Amount, Payment_Method, Payment_Status, Paid_At) VALUES (8, 595.20, 'NETBANKING', 'SUCCESS', DATE_SUB(NOW(), INTERVAL 9 DAY));

-- Booking 9: cancelled
INSERT INTO BOOKING (PNR, Train_ID, Source_Station_ID, Destination_Station_ID, Class_Type, Journey_Date, Status, Total_Fare, Contact_Email, Contact_Phone, Booked_At)
VALUES ('1000000009', 3, 1, 9, 'CC', DATE_SUB(CURDATE(), INTERVAL 6 DAY), 'CANCELLED', 278.40, 'divya@example.com', '9999999999', DATE_SUB(NOW(), INTERVAL 7 DAY));
INSERT INTO BOOKING_PASSENGER VALUES (9, 10);
INSERT INTO SEAT_RESERVATION (Booking_ID, Passenger_ID, Seat_ID, Journey_Date, Status, Active_Flag) VALUES (9, 10, 361, DATE_SUB(CURDATE(), INTERVAL 6 DAY), 'CANCELLED', NULL);
INSERT INTO PAYMENT (Booking_ID, Amount, Payment_Method, Payment_Status, Paid_At) VALUES (9, 278.40, 'UPI', 'REFUNDED', DATE_SUB(NOW(), INTERVAL 6 DAY));
INSERT INTO CANCELLATION (Booking_ID, Cancelled_At, Reason, Refund_Amount) VALUES (9, DATE_SUB(NOW(), INTERVAL 6 DAY), 'Medical emergency', 222.72);

-- Booking 10: confirmed, 3 passengers
INSERT INTO BOOKING (PNR, Train_ID, Source_Station_ID, Destination_Station_ID, Class_Type, Journey_Date, Status, Total_Fare, Contact_Email, Contact_Phone, Booked_At)
VALUES ('1000000010', 1, 1, 8, '3A', DATE_SUB(CURDATE(), INTERVAL 4 DAY), 'CONFIRMED', 2661.60, 'raju@example.com', '9100000000', DATE_SUB(NOW(), INTERVAL 5 DAY));
INSERT INTO BOOKING_PASSENGER VALUES (10, 11), (10, 12), (10, 13);
INSERT INTO SEAT_RESERVATION (Booking_ID, Passenger_ID, Seat_ID, Journey_Date, Status, Active_Flag) VALUES
(10, 11, 146, DATE_SUB(CURDATE(), INTERVAL 4 DAY), 'ACTIVE', 1),
(10, 12, 147, DATE_SUB(CURDATE(), INTERVAL 4 DAY), 'ACTIVE', 1),
(10, 13, 148, DATE_SUB(CURDATE(), INTERVAL 4 DAY), 'ACTIVE', 1);
INSERT INTO PAYMENT (Booking_ID, Amount, Payment_Method, Payment_Status, Paid_At) VALUES (10, 2661.60, 'CARD', 'SUCCESS', DATE_SUB(NOW(), INTERVAL 5 DAY));

-- Booking 11: FAILED
INSERT INTO BOOKING (PNR, Train_ID, Source_Station_ID, Destination_Station_ID, Class_Type, Journey_Date, Status, Total_Fare, Contact_Email, Contact_Phone, Booked_At)
VALUES ('1000000011', 2, 1, 8, '2A', DATE_SUB(CURDATE(), INTERVAL 3 DAY), 'FAILED', 1330.80, 'anita@example.com', '9111111112', DATE_SUB(NOW(), INTERVAL 3 DAY));
INSERT INTO BOOKING_PASSENGER VALUES (11, 14);
INSERT INTO PAYMENT (Booking_ID, Amount, Payment_Method, Payment_Status, Paid_At) VALUES (11, 1330.80, 'CARD', 'FAILED', DATE_SUB(NOW(), INTERVAL 3 DAY));

-- Booking 12: confirmed
INSERT INTO BOOKING (PNR, Train_ID, Source_Station_ID, Destination_Station_ID, Class_Type, Journey_Date, Status, Total_Fare, Contact_Email, Contact_Phone, Booked_At)
VALUES ('1000000012', 1, 4, 8, 'SL', DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'CONFIRMED', 195.20, 'kiran@example.com', '9222222221', DATE_SUB(NOW(), INTERVAL 2 DAY));
INSERT INTO BOOKING_PASSENGER VALUES (12, 15);
INSERT INTO SEAT_RESERVATION (Booking_ID, Passenger_ID, Seat_ID, Journey_Date, Status, Active_Flag) VALUES (12, 15, 6, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'ACTIVE', 1);
INSERT INTO PAYMENT (Booking_ID, Amount, Payment_Method, Payment_Status, Paid_At) VALUES (12, 195.20, 'UPI', 'SUCCESS', DATE_SUB(NOW(), INTERVAL 2 DAY));

-- Booking 13: confirmed
INSERT INTO BOOKING (PNR, Train_ID, Source_Station_ID, Destination_Station_ID, Class_Type, Journey_Date, Status, Total_Fare, Contact_Email, Contact_Phone, Booked_At)
VALUES ('1000000013', 2, 1, 6, 'SL', DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'CONFIRMED', 295.20, 'deepa@example.com', '9333333332', DATE_SUB(NOW(), INTERVAL 1 DAY));
INSERT INTO BOOKING_PASSENGER VALUES (13, 16);
INSERT INTO SEAT_RESERVATION (Booking_ID, Passenger_ID, Seat_ID, Journey_Date, Status, Active_Flag) VALUES (13, 16, 7, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'ACTIVE', 1);
INSERT INTO PAYMENT (Booking_ID, Amount, Payment_Method, Payment_Status, Paid_At) VALUES (13, 295.20, 'CASH', 'SUCCESS', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- Booking 14: confirmed, future
INSERT INTO BOOKING (PNR, Train_ID, Source_Station_ID, Destination_Station_ID, Class_Type, Journey_Date, Status, Total_Fare, Contact_Email, Contact_Phone, Booked_At)
VALUES ('1000000014', 1, 1, 8, 'SL', DATE_ADD(CURDATE(), INTERVAL 10 DAY), 'CONFIRMED', 595.20, 'sanjay@example.com', '9444444443', DATE_SUB(NOW(), INTERVAL 1 DAY));
INSERT INTO BOOKING_PASSENGER VALUES (14, 17);
INSERT INTO SEAT_RESERVATION (Booking_ID, Passenger_ID, Seat_ID, Journey_Date, Status, Active_Flag) VALUES (14, 17, 8, DATE_ADD(CURDATE(), INTERVAL 10 DAY), 'ACTIVE', 1);
INSERT INTO PAYMENT (Booking_ID, Amount, Payment_Method, Payment_Status, Paid_At) VALUES (14, 595.20, 'UPI', 'SUCCESS', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- Booking 15: confirmed, future
INSERT INTO BOOKING (PNR, Train_ID, Source_Station_ID, Destination_Station_ID, Class_Type, Journey_Date, Status, Total_Fare, Contact_Email, Contact_Phone, Booked_At)
VALUES ('1000000015', 5, 1, 8, '2A', DATE_ADD(CURDATE(), INTERVAL 15 DAY), 'CONFIRMED', 1330.80, 'nisha@example.com', '9555555554', NOW());
INSERT INTO BOOKING_PASSENGER VALUES (15, 18);
INSERT INTO SEAT_RESERVATION (Booking_ID, Passenger_ID, Seat_ID, Journey_Date, Status, Active_Flag) VALUES (15, 18, 913, DATE_ADD(CURDATE(), INTERVAL 15 DAY), 'ACTIVE', 1);
INSERT INTO PAYMENT (Booking_ID, Amount, Payment_Method, Payment_Status, Paid_At) VALUES (15, 1330.80, 'CARD', 'SUCCESS', NOW());

-- ============================================================
-- VERIFICATION QUERIES (run manually to check)
-- ============================================================
-- SELECT COUNT(*) FROM STATION;   -- expect 12
-- SELECT COUNT(*) FROM SEAT;      -- expect 1352 approx
-- SELECT t.Train_Name, COUNT(*) AS stops FROM TRAIN t JOIN TRAIN_STATION ts USING(Train_ID) GROUP BY t.Train_ID, t.Train_Name;
-- SELECT COUNT(*) FROM BOOKING;   -- expect 15
