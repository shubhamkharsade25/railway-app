package com.railway.controller;

import com.railway.dto.BookingRequest;
import com.railway.dto.BookingResponse;
import com.railway.dto.TicketDto;
import com.railway.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody BookingRequest req) {
        BookingResponse response = bookingService.createBooking(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{pnr}")
    public ResponseEntity<BookingResponse> getByPnr(@PathVariable String pnr) {
        return ResponseEntity.ok(bookingService.getByPnr(pnr));
    }

    @GetMapping("/{pnr}/ticket")
    public ResponseEntity<TicketDto> getTicket(@PathVariable String pnr) {
        return ResponseEntity.ok(bookingService.getTicket(pnr));
    }

    @GetMapping("/{pnr}/payment")
    public ResponseEntity<?> getPayment(@PathVariable String pnr) {
        BookingResponse booking = bookingService.getByPnr(pnr);
        return ResponseEntity.ok(Map.of(
                "pnr", booking.pnr(),
                "payment", booking.payment()
        ));
    }
}
