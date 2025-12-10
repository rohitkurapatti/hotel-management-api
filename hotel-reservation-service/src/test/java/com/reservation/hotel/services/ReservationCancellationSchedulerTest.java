package com.reservation.hotel.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationCancellationSchedulerTest {

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private ReservationCancellationScheduler scheduler;

    @Test
    void testAutoCancelPendingReservationsShouldCancelReservationsSuccessfully() {
        int expectedCancelledCount = 5;
        when(reservationService.cancelPendingBankTransferReservations())
                .thenReturn(expectedCancelledCount);

        scheduler.autoCancelPendingReservations();

        verify(reservationService, times(1)).cancelPendingBankTransferReservations();
        // Verify MDC is cleared after execution
        assertNull(MDC.get("traceId"));
    }

    @Test
    void testAutoCancelPendingReservationsShouldCleanUpMDCEvenOnException() {
        when(reservationService.cancelPendingBankTransferReservations())
                .thenThrow(new RuntimeException("Test exception"));

        try {
            scheduler.autoCancelPendingReservations();
        } catch (RuntimeException e) {
            // Expected exception
        }

        // Verify MDC is cleared even when exception occurs
        assertNull(MDC.get("traceId"));
    }
}

