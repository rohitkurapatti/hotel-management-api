package com.reservation.hotel.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    }
}

