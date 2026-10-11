# Railway Management System (Academic DBMS Project)

An end-to-end Railway Reservation & Management System built for an academic DBMS project. Emphasizes clean relational design, ACID transactions, raw SQL queries through Spring JDBC `JdbcTemplate`, and a modern React + Vite user interface.

---

## 🛠️ Tech Stack

- **Frontend**: React 18, Vite, React Router v6, Lucide React icons, Custom CSS Design System
- **Backend**: Java 21 / 25, Spring Boot 3.3.x (Maven)
- **Database Access**: Spring `JdbcTemplate` & `NamedParameterJdbcTemplate` with raw SQL — **strictly no JPA, Hibernate, or ORM**
- **Database**: MySQL 8.x (InnoDB engine)
- **Security / Auth**: None (Academic demonstration specification)
- **Payments**: Simulated payment workflow (`SUCCESS` / `FAILED` / `PENDING`)

---

## 📁 Project Architecture

```
Railway_app/
├── railway-backend/
│   ├── db/
│   │   ├── 01_schema.sql      # 12 InnoDB tables with constraints
│   │   ├── 02_seed.sql        # Sample stations, trains, routes, seats, bookings
│   │   ├── 03_views.sql       # v_train_route, v_booking_summary, v_seat_availability_base
│   │   └── 04_indexes.sql     # Query optimization indexes
│   ├── src/main/java/com/railway/
│   │   ├── config/            # WebConfig (CORS), QueryLabCatalog
│   │   ├── controller/        # REST Controllers (Health, Station, Train, Booking, Report)
│   │   ├── dto/               # Java records for requests/responses
│   │   ├── exception/         # Custom exceptions & GlobalExceptionHandler
│   │   ├── repository/        # Pure JdbcTemplate queries
│   │   └── service/           # @Transactional business logic
│   └── pom.xml
├── railway-frontend/
│   ├── src/
│   │   ├── api/client.js      # Central fetch API client
│   │   ├── components/        # Navbar, Footer
│   │   ├── pages/             # Home, Results, Book, TicketView, PnrStatus, Admin, Reports, QueryLab
│   │   └── index.css          # Design system & print styles
│   └── package.json
└── docs/
    ├── schema_and_3nf.md      # Relational schema & 3NF justifications
    ├── viva_questions.md      # 10 core technical evaluation Q&As
    └── explain_plans.txt      # Execution plans for search & availability
```

---

## 🚀 Getting Started

### Prerequisites
- Java JDK 21+
- Apache Maven 3.8+
- Node.js 18+ & npm
- MySQL 8.0+

### 1. Database Setup
Run the SQL scripts in this exact order:
```bash
mysql -u root -p
```
```sql
CREATE DATABASE IF NOT EXISTS railway_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'railway_app'@'localhost' IDENTIFIED BY 'root';
GRANT ALL PRIVILEGES ON railway_db.* TO 'railway_app'@'localhost';
FLUSH PRIVILEGES;
```

Then execute the four schema scripts:
```bash
mysql -u railway_app -proot railway_db < railway-backend/db/01_schema.sql
mysql -u railway_app -proot railway_db < railway-backend/db/02_seed.sql
mysql -u railway_app -proot railway_db < railway-backend/db/04_indexes.sql
mysql -u railway_app -proot railway_db < railway-backend/db/03_views.sql
```

### 2. Configure Environment Variables
Set database credentials in your terminal or profile:
```bash
export DB_USERNAME=railway_app
export DB_PASSWORD=root
```

### 3. Run the Backend
```bash
cd railway-backend
mvn spring-boot:run
```
Backend starts on **`http://localhost:8080`**. Test health endpoint:
```bash
curl http://localhost:8080/api/health
# {"status":"UP","db":"OK"}
```

### 4. Run the Frontend
In a new terminal window:
```bash
cd railway-frontend
npm install
npm run dev
```
Open **`http://localhost:5173`** in your browser.

---

## 🧪 Testing

Run all unit, integration, and concurrency tests:
```bash
cd railway-backend
mvn test
```
The test suite validates:
- MockMvc standalone controllers
- Global exception mappings (400, 404, 409)
- Read-only SQL safety assertions on Query Lab
- **Concurrency test**: Multi-threaded simulated simultaneous booking requests for inventory safety

---

## 📑 Core REST API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/health` | Healthcheck (`SELECT 1` via `JdbcTemplate`) |
| `GET` | `/api/stations` | List stations (supports `?q=`) |
| `POST` | `/api/stations` | Create new station |
| `GET` | `/api/trains` | List active trains |
| `GET` | `/api/trains/{id}/route` | List ordered stops for a train route |
| `GET` | `/api/trains/search` | Directional search (`?source=&destination=&date=`) |
| `GET` | `/api/trains/{id}/availability` | Real-time seat inventory check |
| `POST` | `/api/bookings` | Book ticket (`@Transactional` seat allocation) |
| `GET` | `/api/bookings/{pnr}` | Booking details by PNR |
| `GET` | `/api/bookings/{pnr}/ticket` | E-ticket slip details |
| `POST` | `/api/bookings/{pnr}/cancel` | Cancel booking & calculate refund |
| `GET` | `/api/reports/summary` | High-level metrics & financial summary |
| `GET` | `/api/reports/revenue` | 30-day net revenue breakdown |
| `GET` | `/api/query-lab` | Whitelisted DBMS educational query catalog |
| `GET` | `/api/query-lab/{id}/result` | Execute Query Lab SQL query |

---

## 💡 Key Design Decisions & Viva Highlights

1. **No ORM / Spring Data JPA**: All database queries are hand-written SQL in repository classes for predictable execution plans and clear academic demonstration.
2. **Double-Booking Safeguard**:
   - `SELECT ... FOR UPDATE SKIP LOCKED` inside Spring transactions.
   - Partial unique index: `UNIQUE KEY uq_active_seat (Seat_ID, Journey_Date, Active_Flag)` on `SEAT_RESERVATION`. Active reservations set `Active_Flag=1`; cancelled reservations set `Active_Flag=NULL`.
3. **Directional Train Search**: Uses a self-join on `TRAIN_STATION` enforcing `src.Stop_No < dst.Stop_No`.
4. **Refund Policy**:
   - $> 48\text{ hours}$ before departure: 90% refund
   - $12\text{ to }48\text{ hours}$: 50% refund
   - $< 12\text{ hours}$: 0% refund
5. **Interactive SQL Query Lab**: 11 curated queries demonstrating `INNER JOIN`, `LEFT JOIN`, `GROUP BY`, `HAVING`, `NOT EXISTS`, `EXPLAIN`, `RANK() OVER (...)`, and `WITH (CTE)` syntax.
