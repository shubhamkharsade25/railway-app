# Railway Management System — Viva & Technical Q&A Bank

### 1. Why use Spring JDBC / JdbcTemplate rather than an ORM like Hibernate or Spring Data JPA?
- **Full Control & Optimization**: Raw SQL enables fine-grained performance tuning, execution plan verification, and exact index usage without JPA query translation overhead or N+1 query surprises.
- **Academic Transparency**: Demonstrates core relational DBMS concepts (self-joins, views, window functions, locking syntax) directly in application code rather than abstracting them behind an ORM.
- **Resource Efficiency**: `JdbcTemplate` provides thread-safe connection leasing via HikariCP with minimal memory consumption compared to Hibernate's persistence context and dirty-checking mechanisms.

### 2. Why prepared statements and parameterized `?` / named parameter placeholders?
- **SQL Injection Prevention**: Binds user input safely as data literals rather than executable SQL syntax.
- **Plan Caching**: Enables the MySQL database engine to parse, optimize, and reuse cached query execution plans across queries with different parameters.

### 3. Explain the train search query's self-join and why `Stop_No` ordering matters.
- A single train visits multiple stations along its route. To determine if Train $T$ travels from Station $A$ to Station $B$, we join `TRAIN_STATION` with itself:
  ```sql
  JOIN TRAIN_STATION src ON src.Train_ID = t.Train_ID AND src.Station_ID = ?
  JOIN TRAIN_STATION dst ON dst.Train_ID = t.Train_ID AND dst.Station_ID = ?
  WHERE src.Stop_No < dst.Stop_No AND t.Is_Active = 1
  ```
- **Stop_No check**: Ensures directional validity ($A$ must be visited before $B$ on the route schedule), preventing reversed routes from showing up.

### 4. How do you prevent two users from booking the exact same seat concurrently?
Two distinct layers of concurrency defense are deployed:
1. **Pessimistic Locking**: `SELECT ... FOR UPDATE SKIP LOCKED` inside a Spring `@Transactional` service boundary. This locks candidate free seats during the active transaction so concurrent threads don't read the same rows.
2. **Database Unique Constraint with NULL Trick**:
   ```sql
   UNIQUE KEY uq_active_seat (Seat_ID, Journey_Date, Active_Flag)
   ```
   Active bookings set `Active_Flag = 1`. In MySQL, multiple `NULL` values are permitted in a unique index, but duplicate non-NULL combinations are rejected. If a cancelled ticket sets `Active_Flag = NULL`, the seat is freed instantly without losing the historic reservation record. Two concurrent active reservations for the same seat on the same journey date trigger an instant `DuplicateKeyException` (409 Conflict).

### 5. What are the ACID properties and where do they appear in the booking lifecycle?
- **Atomicity**: The booking process writes across 5 tables (`BOOKING`, `PASSENGER`, `BOOKING_PASSENGER`, `SEAT_RESERVATION`, `PAYMENT`). If seat reservation or payment simulation fails, the whole operation rolls back completely with zero orphaned records.
- **Consistency**: Enforced through foreign key constraints (`ON DELETE RESTRICT`), checked conditions (`Age BETWEEN 0 AND 120`, `Stop_No > 0`), and unique PNR constraints.
- **Isolation**: Handled via InnoDB default repeatable read with `FOR UPDATE` pessimistic row locks, preventing race conditions.
- **Durability**: Commits are persisted to MySQL InnoDB redo logs on disk upon transaction completion.

### 6. Which Normal Form is the database in? Is there any intentional denormalization?
- The database strictly satisfies **Third Normal Form (3NF)**:
  - All attributes depend on primary keys (1NF & 2NF).
  - No transitive dependencies (e.g., stations, coaches, fares each have dedicated tables).
- **Intentional Snapshot Denormalization**: `BOOKING.Total_Fare` is captured at booking time. If station distances or `CLASS_FARE` rates are altered later, historic financial statements and booked tickets remain immutable.

### 7. Why is `ON DELETE RESTRICT` used on parent booking records?
- Financial and audit integrity. An administrator should never be able to accidentally drop a `TRAIN` or `STATION` that has active customer bookings or historical revenue attached.

### 8. What is the difference between a database View and a Table, and where are views used?
- A **Table** stores raw data on disk. A **View** is a saved, named query that is executed dynamically on demand.
- In this project, `v_train_route`, `v_booking_summary`, and `v_seat_availability_base` abstract complex multi-table joins into reusable database objects.

### 9. Why is the SQL Query Lab whitelisted?
- Security: Allowing arbitrary SQL strings from the frontend would expose the database to arbitrary commands (`DROP TABLE`, unauthorized writes).
- The Query Lab uses a server-side catalog of predefined educational queries. Every query starts strictly with `SELECT`, `WITH`, or `EXPLAIN`.

### 10. How are refunds computed and how is revenue calculated?
- **Refund Policy Tiers**:
  - $> 48\text{ h}$ before departure: 90% refund.
  - $12\text{ h} - 48\text{ h}$: 50% refund.
  - $< 12\text{ h}$: 0% refund.
- **Net Revenue**: $\sum(\text{Successful Payments}) - \sum(\text{Refunds})$. Cancelled or failed payments are never double-counted.
