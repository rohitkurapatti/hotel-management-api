package com.reservation.hotel.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "scheduler.reservation-cancellation.enabled", havingValue = "true", matchIfMissing = true)
public class ReservationCancellationScheduler {

    private static final String TRACE_ID_MDC_KEY = "traceId";

    private final ReservationService reservationService;

    @Scheduled(cron = "${scheduler.reservation-cancellation.cron}")
    public void autoCancelPendingReservations() {
        // Generate trace-id for scheduler execution
        String traceId = UUID.randomUUID().toString();

        try {
            MDC.put(TRACE_ID_MDC_KEY, traceId);
            log.info("Running auto-cancellation scheduler with trace-id: {}", traceId);

            int cancelled = reservationService.cancelPendingBankTransferReservations();
        } finally {
            // Clean up MDC to prevent memory leaks
            MDC.clear();
        }
    }
}

