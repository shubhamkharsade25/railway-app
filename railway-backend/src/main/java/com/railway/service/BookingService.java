package com.railway.service;

import com.railway.dto.*;
import com.railway.exception.*;
import com.railway.repository.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class BookingService {

    private static final Set<String> VALID_CLASSES = Set.of("SL", "3A", "2A", "1A", "CC");
    private static final int MAX_ADVANCE_DAYS = 120;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final BookingRepository bookingRepository;
    private final TrainRepository trainRepository;
    private final StationRepository stationRepository;
    private final AvailabilityRepository availabilityRepository;
    private final SearchService searchService;

    public BookingService(BookingRepository bookingRepository, TrainRepository trainRepository,
                          StationRepository stationRepository, AvailabilityRepository availabilityRepository,
                          SearchService searchService) {
        this.bookingRepository = bookingRepository;
        this.trainRepository = trainRepository;
        this.stationRepository = stationRepository;
        this.availabilityRepository = availabilityRepository;
        this.searchService = searchService;
    }

    /**
     * Main booking transaction. Steps:
     * 1. Validate business rules
     * 2. Lock seats with FOR UPDATE SKIP LOCKED
     * 3. Insert BOOKING with generated PNR
     * 4. Insert PASSENGER + BOOKING_PASSENGER + SEAT_RESERVATION per passenger
     * 5. Insert PAYMENT
     * 6. If payment FAILED: mark booking FAILED, cancel reservations
     */
    @Transactional
    public BookingResponse createBooking(BookingRequest req) {
        // --- Business validation ---
        if (!trainRepository.isActive(req.trainId())) {
            throw new ResourceNotFoundException("Train not found or inactive: " + req.trainId());
        }
        if (!stationRepository.existsById(req.sourceStationId())) {
            throw new ResourceNotFoundException("Source station not found: " + req.sourceStationId());
        }
        if (!stationRepository.existsById(req.destinationStationId())) {
            throw new ResourceNotFoundException("Destination station not found: " + req.destinationStationId());
        }

        LocalDate today = LocalDate.now();
        if (req.journeyDate().isBefore(today)) {
            throw new BadRequestException("Journey date cannot be in the past");
        }
        if (req.journeyDate().isAfter(today.plusDays(MAX_ADVANCE_DAYS))) {
            throw new BadRequestException("Journey date cannot be more than " + MAX_ADVANCE_DAYS + " days in advance");
        }

        if (!availabilityRepository.classExistsOnTrain(req.trainId(), req.classType())) {
            throw new BadRequestException("Class " + req.classType() + " is not available on this train");
        }

        // --- Lock available seats ---
        int passengerCount = req.passengers().size();
        List<Integer> lockedSeatIds = bookingRepository.lockAvailableSeats(
                req.trainId(), req.classType(), req.journeyDate(), passengerCount);

        if (lockedSeatIds.size() < passengerCount) {
            throw new InsufficientSeatsException(
                    "Only " + lockedSeatIds.size() + " seats available but " + passengerCount + " requested");
        }

        // --- Calculate fare ---
        BigDecimal farePerPassenger = searchService.calculateFare(req.classType(),
                getDistanceBetweenStations(req.trainId(), req.sourceStationId(), req.destinationStationId()));
        BigDecimal totalFare = farePerPassenger.multiply(BigDecimal.valueOf(passengerCount));

        // --- Generate PNR with retry ---
        String pnr = generateUniquePnr();

        // --- Insert BOOKING ---
        int bookingId;
        try {
            bookingId = bookingRepository.insertBooking(
                    pnr, req.trainId(), req.sourceStationId(), req.destinationStationId(),
                    req.classType(), req.journeyDate(), "CONFIRMED", totalFare,
                    req.contactEmail(), req.contactPhone());
        } catch (DuplicateKeyException e) {
            throw new ConflictException("PNR collision — please retry");
        }

        // --- Insert passengers and reservations ---
        List<BookingResponse.PassengerTicketInfo> passengerInfos = new ArrayList<>();
        for (int i = 0; i < passengerCount; i++) {
            PassengerRequest p = req.passengers().get(i);
            int passengerId = bookingRepository.insertPassenger(p.name(), p.age(), p.gender());
            bookingRepository.insertBookingPassenger(bookingId, passengerId);

            int seatId = lockedSeatIds.get(i);
            try {
                bookingRepository.insertSeatReservation(bookingId, passengerId, seatId, req.journeyDate());
            } catch (DuplicateKeyException e) {
                throw new ConflictException("Seat " + seatId + " was just booked by another user");
            }

            // Fetch seat details for response
            Map<String, Object> seatDetails = getSeatDetails(seatId);
            passengerInfos.add(new BookingResponse.PassengerTicketInfo(
                    passengerId, p.name(), p.age(), p.gender(),
                    (String) seatDetails.get("Coach_Number"),
                    ((Number) seatDetails.get("Seat_Number")).intValue(),
                    (String) seatDetails.get("Seat_Type"),
                    seatId
            ));
        }

        // --- Insert PAYMENT ---
        int paymentId = bookingRepository.insertPayment(
                bookingId, totalFare, req.paymentMethod(), req.simulatedPaymentStatus());

        // --- Handle FAILED payment ---
        if ("FAILED".equals(req.simulatedPaymentStatus())) {
            bookingRepository.updateBookingStatus(bookingId, "FAILED");
            bookingRepository.cancelReservations(bookingId);
        }

        // --- Build response ---
        TrainDto train = trainRepository.findById(req.trainId()).orElseThrow();
        StationDto source = stationRepository.findById(req.sourceStationId()).orElseThrow();
        StationDto dest = stationRepository.findById(req.destinationStationId()).orElseThrow();

        String finalStatus = "FAILED".equals(req.simulatedPaymentStatus()) ? "FAILED" : "CONFIRMED";

        return new BookingResponse(
                bookingId, pnr, finalStatus,
                train.trainId(), train.trainNumber(), train.trainName(),
                source.stationId(), source.stationName(),
                dest.stationId(), dest.stationName(),
                req.journeyDate(), req.classType(), totalFare,
                req.contactEmail(), req.contactPhone(),
                java.time.LocalDateTime.now(),
                passengerInfos,
                new BookingResponse.PaymentInfo(paymentId, totalFare, req.paymentMethod(),
                        req.simulatedPaymentStatus(), java.time.LocalDateTime.now())
        );
    }

    /**
     * Get booking by PNR.
     */
    public BookingResponse getByPnr(String pnr) {
        if (!pnr.matches("\\d{10}")) {
            throw new BadRequestException("PNR must be exactly 10 digits");
        }
        Map<String, Object> booking = bookingRepository.findByPnr(pnr)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found for PNR: " + pnr));

        int bookingId = ((Number) booking.get("Booking_ID")).intValue();
        List<Map<String, Object>> passengers = bookingRepository.findPassengersWithSeats(bookingId);
        List<Map<String, Object>> payments = bookingRepository.findPayments(bookingId);

        List<BookingResponse.PassengerTicketInfo> passengerInfos = passengers.stream()
                .map(p -> new BookingResponse.PassengerTicketInfo(
                        ((Number) p.get("Passenger_ID")).intValue(),
                        (String) p.get("Passenger_Name"),
                        ((Number) p.get("Age")).intValue(),
                        (String) p.get("Gender"),
                        (String) p.get("Coach_Number"),
                        p.get("Seat_Number") != null ? ((Number) p.get("Seat_Number")).intValue() : null,
                        (String) p.get("Seat_Type"),
                        p.get("Seat_ID") != null ? ((Number) p.get("Seat_ID")).intValue() : null
                ))
                .toList();

        BookingResponse.PaymentInfo paymentInfo = null;
        if (!payments.isEmpty()) {
            Map<String, Object> pay = payments.get(0);
            paymentInfo = new BookingResponse.PaymentInfo(
                    ((Number) pay.get("Payment_ID")).intValue(),
                    new BigDecimal(pay.get("Amount").toString()),
                    (String) pay.get("Payment_Method"),
                    (String) pay.get("Payment_Status"),
                    toLocalDateTime(pay.get("Paid_At"))
            );
        }

        Object bookedAtRaw = booking.get("Booked_At");
        Object journeyDateRaw = booking.get("Journey_Date");

        return new BookingResponse(
                bookingId,
                (String) booking.get("PNR"),
                (String) booking.get("Status"),
                ((Number) booking.get("Train_ID")).intValue(),
                (String) booking.get("Train_Number"),
                (String) booking.get("Train_Name"),
                ((Number) booking.get("Source_Station_ID")).intValue(),
                (String) booking.get("Source_Station_Name"),
                ((Number) booking.get("Destination_Station_ID")).intValue(),
                (String) booking.get("Dest_Station_Name"),
                toLocalDate(journeyDateRaw),
                (String) booking.get("Class_Type"),
                new BigDecimal(booking.get("Total_Fare").toString()),
                (String) booking.get("Contact_Email"),
                (String) booking.get("Contact_Phone"),
                toLocalDateTime(bookedAtRaw),
                passengerInfos,
                paymentInfo
        );
    }

    /**
     * Get ticket details for a PNR.
     */
    public TicketDto getTicket(String pnr) {
        BookingResponse booking = getByPnr(pnr);

        Map<String, Object> routeTimes = bookingRepository.findRouteTimes(
                booking.trainId(), booking.sourceStationId(), booking.destinationStationId());

        LocalTime departureTime = toLocalTime(routeTimes.get("departure_time"));
        LocalTime arrivalTime = toLocalTime(routeTimes.get("arrival_time"));

        TrainDto train = trainRepository.findById(booking.trainId()).orElseThrow();

        List<TicketDto.PassengerTicketRow> rows = booking.passengers().stream()
                .map(p -> new TicketDto.PassengerTicketRow(
                        p.name(), p.age(), p.gender(),
                        p.coachNumber(), p.seatNumber(), p.seatType()
                ))
                .toList();

        TicketDto.PaymentInfo payInfo = booking.payment() != null
                ? new TicketDto.PaymentInfo(booking.payment().amount(),
                booking.payment().paymentMethod(), booking.payment().paymentStatus())
                : null;

        return new TicketDto(
                booking.pnr(), booking.status(),
                new TicketDto.TrainInfo(train.trainId(), train.trainNumber(), train.trainName(), train.trainType()),
                new TicketDto.StationInfo(booking.sourceStationId(), booking.sourceStationName(), null, departureTime),
                new TicketDto.StationInfo(booking.destinationStationId(), booking.destinationStationName(), null, arrivalTime),
                booking.journeyDate(), booking.classType(), booking.totalFare(),
                booking.contactEmail(), booking.contactPhone(), booking.bookedAt(),
                rows, payInfo
        );
    }

    public List<Map<String, Object>> getAllBookings(String status) {
        return bookingRepository.findAllBookings(status);
    }

    @Transactional
    public BookingResponse updateBooking(String pnr, com.railway.dto.UpdateBookingRequest req) {
        Map<String, Object> booking = bookingRepository.findByPnr(pnr)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found for PNR: " + pnr));

        int bookingId = ((Number) booking.get("Booking_ID")).intValue();

        // If status is transitioning to CANCELLED, free seat reservations
        if ("CANCELLED".equalsIgnoreCase(req.status())) {
            bookingRepository.cancelReservations(bookingId);
        }

        bookingRepository.updateBookingDetails(bookingId, req.status(), req.contactEmail(), req.contactPhone());
        return getByPnr(pnr);
    }

    @Transactional
    public void deleteBooking(String pnr) {
        Map<String, Object> booking = bookingRepository.findByPnr(pnr)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found for PNR: " + pnr));

        int bookingId = ((Number) booking.get("Booking_ID")).intValue();
        bookingRepository.deleteBookingCascade(bookingId);
    }

    // ---- Helpers ----

    private String generateUniquePnr() {
        for (int attempt = 0; attempt < 5; attempt++) {
            long num = (long) (RANDOM.nextDouble() * 9_000_000_000L) + 1_000_000_000L;
            String pnr = String.valueOf(num);
            if (bookingRepository.findByPnr(pnr).isEmpty()) {
                return pnr;
            }
        }
        throw new ConflictException("Could not generate unique PNR after 5 attempts");
    }

    private int getDistanceBetweenStations(int trainId, int sourceStationId, int destStationId) {
        List<RouteStopDto> route = trainRepository.findRoute(trainId);
        int srcDist = -1, dstDist = -1;
        for (RouteStopDto stop : route) {
            if (stop.stationId() == sourceStationId) srcDist = stop.distanceKm();
            if (stop.stationId() == destStationId) dstDist = stop.distanceKm();
        }
        if (srcDist < 0 || dstDist < 0 || dstDist <= srcDist) {
            throw new BadRequestException("Invalid source/destination for this train route");
        }
        return dstDist - srcDist;
    }

    private Map<String, Object> getSeatDetails(int seatId) {
        return availabilityRepository.getSeatDetails(seatId);
    }

    /** Safely convert JDBC date/time objects to LocalDateTime (handles both Timestamp and LocalDateTime). */
    private static java.time.LocalDateTime toLocalDateTime(Object obj) {
        if (obj == null) return null;
        if (obj instanceof java.time.LocalDateTime ldt) return ldt;
        if (obj instanceof java.sql.Timestamp ts) return ts.toLocalDateTime();
        return null;
    }

    /** Safely convert JDBC date objects to LocalDate (handles both java.sql.Date and LocalDate). */
    private static java.time.LocalDate toLocalDate(Object obj) {
        if (obj == null) return null;
        if (obj instanceof java.time.LocalDate ld) return ld;
        if (obj instanceof java.sql.Date d) return d.toLocalDate();
        return null;
    }

    /** Safely convert JDBC time objects to LocalTime (handles both java.sql.Time and LocalTime). */
    private static java.time.LocalTime toLocalTime(Object obj) {
        if (obj == null) return null;
        if (obj instanceof java.time.LocalTime lt) return lt;
        if (obj instanceof java.sql.Time t) return t.toLocalTime();
        return null;
    }
}
