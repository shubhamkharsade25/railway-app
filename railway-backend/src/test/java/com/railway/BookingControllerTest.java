package com.railway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.railway.controller.BookingController;
import com.railway.dto.*;
import com.railway.exception.*;
import com.railway.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Standalone MockMvc tests for BookingController.
 * Uses manual test doubles (no Mockito) to avoid JDK 25 byte-buddy restrictions.
 */
class BookingControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // ---- Manual stubs ----

    static class StubBookingService extends BookingService {
        private BookingResponse stubResponse;
        private RuntimeException stubException;

        StubBookingService() {
            super(null, null, null, null, null);
        }

        void willReturn(BookingResponse r) { this.stubResponse = r; this.stubException = null; }
        void willThrow(RuntimeException e) { this.stubException = e; this.stubResponse = null; }

        @Override
        public BookingResponse createBooking(BookingRequest req) {
            if (stubException != null) throw stubException;
            return stubResponse;
        }

        @Override
        public BookingResponse getByPnr(String pnr) {
            if (stubException != null) throw stubException;
            return stubResponse;
        }

        @Override
        public TicketDto getTicket(String pnr) {
            return null;
        }
    }

    private StubBookingService bookingService;

    @BeforeEach
    void setup() {
        bookingService = new StubBookingService();
        BookingController controller = new BookingController(bookingService);
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(converter)
                .build();
    }

    private BookingRequest validRequest() {
        return new BookingRequest(
                1, 1, 5,
                LocalDate.now().plusDays(5),
                "SL", "test@example.com", "9876543210",
                "UPI", "SUCCESS",
                List.of(new PassengerRequest("Asha", 25, "F", null))
        );
    }

    private BookingResponse sampleResponse() {
        return new BookingResponse(
                1, "1234567890", "CONFIRMED",
                1, "12901", "Gujarat Express (Sample)",
                1, "Mumbai Central",
                5, "Bharuch",
                LocalDate.now().plusDays(5),
                "SL", BigDecimal.valueOf(300),
                "test@example.com", "9876543210",
                LocalDateTime.now(),
                List.of(new BookingResponse.PassengerTicketInfo(1, "Asha", 25, "F", "S1", 5, "LOWER", 10)),
                new BookingResponse.PaymentInfo(1, BigDecimal.valueOf(300), "UPI", "SUCCESS", LocalDateTime.now())
        );
    }

    @Test
    void createBooking_happyPath_returns201() throws Exception {
        bookingService.willReturn(sampleResponse());

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pnr").value("1234567890"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void createBooking_insufficientSeats_returns409() throws Exception {
        bookingService.willThrow(new InsufficientSeatsException("No seats available"));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void createBooking_tooManyPassengers_returns400() throws Exception {
        var req = new BookingRequest(
                1, 1, 5, LocalDate.now().plusDays(5), "SL",
                "a@b.com", "9876543210", "UPI", "SUCCESS",
                List.of(
                        new PassengerRequest("A", 20, "M", null),
                        new PassengerRequest("B", 21, "F", null),
                        new PassengerRequest("C", 22, "M", null),
                        new PassengerRequest("D", 23, "F", null),
                        new PassengerRequest("E", 24, "M", null),
                        new PassengerRequest("F", 25, "F", null),
                        new PassengerRequest("G", 26, "M", null) // 7 — violates @Size(max=6)
                )
        );

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_invalidPhone_returns400() throws Exception {
        var req = new BookingRequest(
                1, 1, 5, LocalDate.now().plusDays(5), "SL",
                "a@b.com", "INVALID", "UPI", "SUCCESS",
                List.of(new PassengerRequest("Asha", 25, "F", null))
        );

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_zeroPassengers_returns400() throws Exception {
        var req = new BookingRequest(
                1, 1, 5, LocalDate.now().plusDays(5), "SL",
                "a@b.com", "9876543210", "UPI", "SUCCESS",
                List.of()
        );

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_pastDate_returns400() throws Exception {
        var req = new BookingRequest(
                1, 1, 5, LocalDate.now().minusDays(1), "SL",
                "a@b.com", "9876543210", "UPI", "SUCCESS",
                List.of(new PassengerRequest("Asha", 25, "F", null))
        );

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getByPnr_invalidFormat_returns400() throws Exception {
        bookingService.willThrow(new BadRequestException("PNR must be exactly 10 digits"));

        mockMvc.perform(get("/api/bookings/INVALID"))
                .andExpect(status().isBadRequest());
    }
}
