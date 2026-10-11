package com.railway.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QueryLabCatalog {

    public record LabQuery(
            String id,
            String title,
            String description,
            String concept,
            String sql
    ) {}

    private static final Map<String, LabQuery> CATALOG = new LinkedHashMap<>();

    static {
        register(new LabQuery(
                "q01-simple-select",
                "Stations List with Sorting",
                "Retrieves all railway stations sorted alphabetically by station name.",
                "Simple SELECT + ORDER BY",
                "SELECT Station_ID, Station_Name, Station_Code, City FROM STATION ORDER BY Station_Name ASC"
        ));

        register(new LabQuery(
                "q02-inner-join",
                "Detailed Train Route Information",
                "Performs an INNER JOIN between train route schedule and stations to get ordered intermediate stops.",
                "INNER JOIN",
                """
                SELECT t.Train_Number, t.Train_Name, ts.Stop_No, s.Station_Name,
                       ts.Arrival_Time, ts.Departure_Time, ts.Distance_Km
                FROM TRAIN_STATION ts
                INNER JOIN TRAIN t ON t.Train_ID = ts.Train_ID
                INNER JOIN STATION s ON s.Station_ID = ts.Station_ID
                WHERE t.Train_ID = 1
                ORDER BY ts.Stop_No ASC
                """
        ));

        register(new LabQuery(
                "q03-self-join",
                "Directional Train Search Logic",
                "Demonstrates a self-join on TRAIN_STATION table ensuring source stop number precedes destination stop number.",
                "Self-Join with Stop Ordering Logic",
                """
                SELECT t.Train_Number, t.Train_Name,
                       src.Departure_Time, dst.Arrival_Time,
                       (dst.Distance_Km - src.Distance_Km) AS Travel_Distance_Km
                FROM TRAIN t
                JOIN TRAIN_STATION src ON src.Train_ID = t.Train_ID AND src.Station_ID = 1
                JOIN TRAIN_STATION dst ON dst.Train_ID = t.Train_ID AND dst.Station_ID = 4
                WHERE src.Stop_No < dst.Stop_No AND t.Is_Active = 1
                """
        ));

        register(new LabQuery(
                "q04-left-join",
                "Trains with or without Bookings",
                "Demonstrates a LEFT OUTER JOIN to list all trains including those with zero passenger bookings.",
                "LEFT OUTER JOIN",
                """
                SELECT t.Train_ID, t.Train_Number, t.Train_Name,
                       COUNT(b.Booking_ID) AS Total_Bookings
                FROM TRAIN t
                LEFT JOIN BOOKING b ON b.Train_ID = t.Train_ID
                GROUP BY t.Train_ID, t.Train_Number, t.Train_Name
                ORDER BY Total_Bookings DESC
                """
        ));

        register(new LabQuery(
                "q05-group-by-having",
                "High Traffic Trains",
                "Groups confirmed bookings by train and applies HAVING filter to find trains with 2 or more bookings.",
                "GROUP BY + HAVING",
                """
                SELECT t.Train_Number, t.Train_Name, COUNT(b.Booking_ID) AS Confirmed_Bookings,
                       SUM(b.Total_Fare) AS Gross_Fare_Sum
                FROM BOOKING b
                JOIN TRAIN t ON t.Train_ID = b.Train_ID
                WHERE b.Status = 'CONFIRMED'
                GROUP BY t.Train_ID, t.Train_Number, t.Train_Name
                HAVING COUNT(b.Booking_ID) >= 2
                ORDER BY Confirmed_Bookings DESC
                """
        ));

        register(new LabQuery(
                "q06-aggregates",
                "System Financial and Volume Metrics",
                "Uses multiple aggregate functions (SUM, AVG, MIN, MAX, COUNT) to evaluate overall passenger statistics.",
                "Aggregate Functions",
                """
                SELECT COUNT(Booking_ID) AS Total_Bookings,
                       SUM(Total_Fare) AS Total_Booking_Revenue,
                       AVG(Total_Fare) AS Average_Ticket_Fare,
                       MIN(Total_Fare) AS Minimum_Fare,
                       MAX(Total_Fare) AS Maximum_Fare
                FROM BOOKING
                WHERE Status = 'CONFIRMED'
                """
        ));

        register(new LabQuery(
                "q07-subquery-not-exists",
                "Available Unreserved Seats",
                "Uses NOT EXISTS correlated subquery to isolate unbooked seats for a train and journey date.",
                "Correlated Subquery with NOT EXISTS",
                """
                SELECT s.Seat_ID, c.Coach_Number, c.Class_Type, s.Seat_Number, s.Seat_Type
                FROM SEAT s
                JOIN COACH c ON c.Coach_ID = s.Coach_ID
                WHERE c.Train_ID = 1 AND c.Class_Type = 'SL'
                  AND NOT EXISTS (
                    SELECT 1 FROM SEAT_RESERVATION sr
                    WHERE sr.Seat_ID = s.Seat_ID
                      AND sr.Journey_Date = CURDATE()
                      AND sr.Status = 'ACTIVE'
                  )
                LIMIT 50
                """
        ));

        register(new LabQuery(
                "q08-view-usage",
                "Querying the Database View (v_booking_summary)",
                "Queries predefined database view v_booking_summary for abstracted reporting without repetitive JOIN syntax.",
                "Database View (v_booking_summary)",
                """
                SELECT PNR, Booking_Status, Train_Name, Source_Station, Destination_Station,
                       Class_Type, Total_Fare, Payment_Status
                FROM v_booking_summary
                ORDER BY Booked_At DESC
                LIMIT 25
                """
        ));

        register(new LabQuery(
                "q09-explain-plan",
                "Query Execution Plan Analysis",
                "Runs EXPLAIN to analyze index utilization and row access paths on the search query.",
                "EXPLAIN Query Plan",
                """
                EXPLAIN
                SELECT t.Train_ID, t.Train_Number, t.Train_Name
                FROM TRAIN t
                JOIN TRAIN_STATION src ON src.Train_ID = t.Train_ID AND src.Station_ID = 1
                JOIN TRAIN_STATION dst ON dst.Train_ID = t.Train_ID AND dst.Station_ID = 4
                WHERE src.Stop_No < dst.Stop_No AND t.Is_Active = 1
                """
        ));

        register(new LabQuery(
                "q10-window-functions",
                "Ranking Popular Routes with Window Functions",
                "Calculates RANK() OVER (...) to order and rank station pairs based on reservation demand.",
                "Window Function (RANK OVER)",
                """
                SELECT ss.Station_Name AS Origin, ds.Station_Name AS Destination,
                       COUNT(b.Booking_ID) AS Total_Trips,
                       RANK() OVER (ORDER BY COUNT(b.Booking_ID) DESC) AS Route_Popularity_Rank
                FROM BOOKING b
                JOIN STATION ss ON ss.Station_ID = b.Source_Station_ID
                JOIN STATION ds ON ds.Station_ID = b.Destination_Station_ID
                GROUP BY ss.Station_Name, ds.Station_Name
                """
        ));

        register(new LabQuery(
                "q11-cte",
                "Common Table Expression (CTE) for Net Earnings",
                "Uses WITH (CTE) syntax to compute gross payments and cancellation deductions before producing the final ledger.",
                "Common Table Expressions (WITH)",
                """
                WITH BookingEarnings AS (
                    SELECT b.Booking_ID, p.Amount AS GrossAmount,
                           COALESCE(c.Refund_Amount, 0) AS RefundDeduction
                    FROM BOOKING b
                    JOIN PAYMENT p ON p.Booking_ID = b.Booking_ID AND p.Payment_Status IN ('SUCCESS', 'REFUNDED')
                    LEFT JOIN CANCELLATION c ON c.Booking_ID = b.Booking_ID
                )
                SELECT COUNT(Booking_ID) AS Transaction_Count,
                       SUM(GrossAmount) AS Total_Gross,
                       SUM(RefundDeduction) AS Total_Refunds,
                       (SUM(GrossAmount) - SUM(RefundDeduction)) AS Net_Realized_Revenue
                FROM BookingEarnings
                """
        ));
    }

    private static void register(LabQuery q) {
        CATALOG.put(q.id(), q);
    }

    public static List<LabQuery> getAll() {
        return Collections.unmodifiableList(List.copyOf(CATALOG.values()));
    }

    public static LabQuery get(String id) {
        return CATALOG.get(id);
    }
}
