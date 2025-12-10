package com.reservation.hotel.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "scheduler.reservation-cancellation.enabled", havingValue = "true", matchIfMissing = true)
public class ReservationCancellationScheduler {

    private final ReservationService reservationService;

    @Scheduled(cron = "${scheduler.reservation-cancellation.cron:0 0 0 * * ?}")
    public void autoCancelPendingReservations() {
        log.info("Running auto-cancellation scheduler");
        int cancelled = reservationService.cancelPendingBankTransferReservations();
        log.info("Auto-cancelled {} reservation(s)", cancelled);
    }
}

