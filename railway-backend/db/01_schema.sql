-- ============================================================
-- 01_schema.sql — Railway Management System
-- Engine: InnoDB | Charset: utf8mb4
-- Re-runnable: drops in reverse FK order first
-- ============================================================

USE railway_db;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS CANCELLATION;
DROP TABLE IF EXISTS PAYMENT;
DROP TABLE IF EXISTS SEAT_RESERVATION;
DROP TABLE IF EXISTS BOOKING_PASSENGER;
DROP TABLE IF EXISTS BOOKING;
DROP TABLE IF EXISTS PASSENGER;
DROP TABLE IF EXISTS CLASS_FARE;
DROP TABLE IF EXISTS SEAT;
DROP TABLE IF EXISTS COACH;
DROP TABLE IF EXISTS TRAIN_STATION;
DROP TABLE IF EXISTS TRAIN;
DROP TABLE IF EXISTS STATION;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. STATION
CREATE TABLE STATION (
    Station_ID   INT          NOT NULL AUTO_INCREMENT,
    Station_Name VARCHAR(100) NOT NULL,
    Station_Code VARCHAR(10)  NOT NULL,
    City         VARCHAR(60)  NOT NULL,
    CONSTRAINT pk_station PRIMARY KEY (Station_ID),
    CONSTRAINT uq_station_code UNIQUE (Station_Code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. TRAIN
CREATE TABLE TRAIN (
    Train_ID     INT          NOT NULL AUTO_INCREMENT,
    Train_Number VARCHAR(10)  NOT NULL,
    Train_Name   VARCHAR(100) NOT NULL,
    Train_Type   ENUM('EXPRESS','SUPERFAST','LOCAL','RAJDHANI','OTHER') NOT NULL DEFAULT 'EXPRESS',
    Is_Active    TINYINT(1)   NOT NULL DEFAULT 1,
    CONSTRAINT pk_train PRIMARY KEY (Train_ID),
    CONSTRAINT uq_train_number UNIQUE (Train_Number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. TRAIN_STATION (junction: train route stops)
CREATE TABLE TRAIN_STATION (
    Train_ID       INT  NOT NULL,
    Station_ID     INT  NOT NULL,
    Stop_No        INT  NOT NULL,
    Arrival_Time   TIME NULL,
    Departure_Time TIME NULL,
    Distance_Km    INT  NOT NULL COMMENT 'Cumulative km from origin',
    CONSTRAINT pk_train_station PRIMARY KEY (Train_ID, Stop_No),
    CONSTRAINT uq_train_station_stop UNIQUE (Train_ID, Station_ID),
    CONSTRAINT chk_stop_no CHECK (Stop_No > 0),
    CONSTRAINT fk_ts_train   FOREIGN KEY (Train_ID)   REFERENCES TRAIN   (Train_ID)   ON DELETE RESTRICT,
    CONSTRAINT fk_ts_station FOREIGN KEY (Station_ID) REFERENCES STATION (Station_ID) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. COACH
CREATE TABLE COACH (
    Coach_ID     INT         NOT NULL AUTO_INCREMENT,
    Train_ID     INT         NOT NULL,
    Coach_Number VARCHAR(5)  NOT NULL,
    Class_Type   ENUM('SL','3A','2A','1A','CC') NOT NULL,
    CONSTRAINT pk_coach PRIMARY KEY (Coach_ID),
    CONSTRAINT uq_coach_train_number UNIQUE (Train_ID, Coach_Number),
    CONSTRAINT fk_coach_train FOREIGN KEY (Train_ID) REFERENCES TRAIN (Train_ID) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. SEAT
CREATE TABLE SEAT (
    Seat_ID     INT NOT NULL AUTO_INCREMENT,
    Coach_ID    INT NOT NULL,
    Seat_Number INT NOT NULL,
    Seat_Type   ENUM('LOWER','MIDDLE','UPPER','SIDE_LOWER','SIDE_UPPER','WINDOW','AISLE') NOT NULL,
    CONSTRAINT pk_seat PRIMARY KEY (Seat_ID),
    CONSTRAINT uq_seat_coach_number UNIQUE (Coach_ID, Seat_Number),
    CONSTRAINT fk_seat_coach FOREIGN KEY (Coach_ID) REFERENCES COACH (Coach_ID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. CLASS_FARE
CREATE TABLE CLASS_FARE (
    Class_Type   ENUM('SL','3A','2A','1A','CC') NOT NULL,
    Rate_Per_Km  DECIMAL(6,2)  NOT NULL,
    Base_Fare    DECIMAL(8,2)  NOT NULL,
    CONSTRAINT pk_class_fare PRIMARY KEY (Class_Type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. PASSENGER
CREATE TABLE PASSENGER (
    Passenger_ID   INT          NOT NULL AUTO_INCREMENT,
    Passenger_Name VARCHAR(100) NOT NULL,
    Age            INT          NOT NULL,
    Gender         ENUM('M','F','O') NOT NULL,
    CONSTRAINT pk_passenger PRIMARY KEY (Passenger_ID),
    CONSTRAINT chk_age CHECK (Age BETWEEN 0 AND 120)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. BOOKING
CREATE TABLE BOOKING (
    Booking_ID             INT            NOT NULL AUTO_INCREMENT,
    PNR                    CHAR(10)       NOT NULL,
    Train_ID               INT            NOT NULL,
    Source_Station_ID      INT            NOT NULL,
    Destination_Station_ID INT            NOT NULL,
    Class_Type             ENUM('SL','3A','2A','1A','CC') NOT NULL,
    Journey_Date           DATE           NOT NULL,
    Booked_At              DATETIME       NOT NULL DEFAULT NOW(),
    Status                 ENUM('CONFIRMED','CANCELLED','FAILED') NOT NULL DEFAULT 'CONFIRMED',
    Total_Fare             DECIMAL(10,2)  NOT NULL,
    Contact_Email          VARCHAR(120)   NOT NULL,
    Contact_Phone          VARCHAR(15)    NOT NULL,
    CONSTRAINT pk_booking PRIMARY KEY (Booking_ID),
    CONSTRAINT uq_pnr UNIQUE (PNR),
    CONSTRAINT fk_booking_train    FOREIGN KEY (Train_ID)               REFERENCES TRAIN   (Train_ID)   ON DELETE RESTRICT,
    CONSTRAINT fk_booking_src_stn  FOREIGN KEY (Source_Station_ID)      REFERENCES STATION (Station_ID) ON DELETE RESTRICT,
    CONSTRAINT fk_booking_dst_stn  FOREIGN KEY (Destination_Station_ID) REFERENCES STATION (Station_ID) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. BOOKING_PASSENGER
CREATE TABLE BOOKING_PASSENGER (
    Booking_ID   INT NOT NULL,
    Passenger_ID INT NOT NULL,
    CONSTRAINT pk_booking_passenger PRIMARY KEY (Booking_ID, Passenger_ID),
    CONSTRAINT fk_bp_booking   FOREIGN KEY (Booking_ID)   REFERENCES BOOKING   (Booking_ID)   ON DELETE RESTRICT,
    CONSTRAINT fk_bp_passenger FOREIGN KEY (Passenger_ID) REFERENCES PASSENGER (Passenger_ID) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. SEAT_RESERVATION
-- Active_Flag = 1 when ACTIVE, NULL when CANCELLED
-- The partial unique index (uq_active_seat) allows many NULLs but blocks two ACTIVE rows for same seat+date
CREATE TABLE SEAT_RESERVATION (
    Reservation_ID INT  NOT NULL AUTO_INCREMENT,
    Booking_ID     INT  NOT NULL,
    Passenger_ID   INT  NOT NULL,
    Seat_ID        INT  NOT NULL,
    Journey_Date   DATE NOT NULL,
    Status         ENUM('ACTIVE','CANCELLED') NOT NULL DEFAULT 'ACTIVE',
    Active_Flag    TINYINT NULL COMMENT '1 = active; NULL = cancelled (allows unique index trick)',
    CONSTRAINT pk_seat_reservation PRIMARY KEY (Reservation_ID),
    CONSTRAINT uq_active_seat UNIQUE KEY (Seat_ID, Journey_Date, Active_Flag),
    CONSTRAINT fk_sr_booking   FOREIGN KEY (Booking_ID)   REFERENCES BOOKING   (Booking_ID)   ON DELETE RESTRICT,
    CONSTRAINT fk_sr_passenger FOREIGN KEY (Passenger_ID) REFERENCES PASSENGER (Passenger_ID) ON DELETE RESTRICT,
    CONSTRAINT fk_sr_seat      FOREIGN KEY (Seat_ID)      REFERENCES SEAT      (Seat_ID)      ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. PAYMENT
CREATE TABLE PAYMENT (
    Payment_ID     INT           NOT NULL AUTO_INCREMENT,
    Booking_ID     INT           NOT NULL,
    Amount         DECIMAL(10,2) NOT NULL,
    Payment_Method ENUM('UPI','CARD','NETBANKING','CASH') NOT NULL,
    Payment_Status ENUM('SUCCESS','FAILED','PENDING','REFUNDED') NOT NULL DEFAULT 'PENDING',
    Paid_At        DATETIME      NULL,
    CONSTRAINT pk_payment PRIMARY KEY (Payment_ID),
    CONSTRAINT fk_payment_booking FOREIGN KEY (Booking_ID) REFERENCES BOOKING (Booking_ID) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. CANCELLATION
CREATE TABLE CANCELLATION (
    Cancellation_ID INT           NOT NULL AUTO_INCREMENT,
    Booking_ID      INT           NOT NULL,
    Cancelled_At    DATETIME      NOT NULL DEFAULT NOW(),
    Reason          VARCHAR(255)  NULL,
    Refund_Amount   DECIMAL(10,2) NULL,
    CONSTRAINT pk_cancellation PRIMARY KEY (Cancellation_ID),
    CONSTRAINT uq_cancellation_booking UNIQUE (Booking_ID),
    CONSTRAINT fk_cancellation_booking FOREIGN KEY (Booking_ID) REFERENCES BOOKING (Booking_ID) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
