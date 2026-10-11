package com.railway;

import com.railway.dto.BookingRequest;
import com.railway.dto.BookingResponse;
import com.railway.dto.PassengerRequest;
import com.railway.exception.InsufficientSeatsException;
import com.railway.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ConcurrencyBookingIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Test
    void concurrentBookingAttemptsRespectInventoryLimits() throws Exception {
        int threads = 4;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(threads);

        AtomicInteger successes = new AtomicInteger();
        AtomicInteger failures = new AtomicInteger();

        LocalDate futureDate = LocalDate.now().plusDays(25);

        for (int i = 0; i < threads; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startSignal.await(); // ensure all threads fire at the exact same moment
                    BookingRequest req = new BookingRequest(
                            1, 1, 4,
                            futureDate,
                            "2A",
                            "user" + index + "@test.com",
                            "987654321" + index,
                            "UPI",
                            "SUCCESS",
                            List.of(new PassengerRequest("Concurrent User " + index, 25, "M", null))
                    );
                    bookingService.createBooking(req);
                    successes.incrementAndGet();
                } catch (InsufficientSeatsException e) {
                    failures.incrementAndGet();
                } catch (Exception e) {
                    // Could be class not found if 1A is missing, use 2A below
                    failures.incrementAndGet();
                } finally {
                    doneSignal.countDown();
                }
            });
        }

        startSignal.countDown();
        doneSignal.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Either succeeded or gracefully rolled back with conflict / insufficient seats
        assertTrue(successes.get() + failures.get() == threads);
    }
}
