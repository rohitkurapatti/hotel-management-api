package com.reservation.hotel.config.kafka;

import com.reservation.hotel.commons.constants.AppConstants;
import com.reservation.hotel.model.BankTransferPaymentEvent;
import com.reservation.hotel.services.ReservationService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@ConditionalOnProperty(
        name = "spring.kafka.enabled",
        havingValue = "true"
)
public class BankTransferPaymentListener {

    private final ReservationService reservationService;

    public BankTransferPaymentListener(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @KafkaListener(
            topics = "bank-transfer-payment-update",
            groupId = "hotel-reservation-consumer")
    public void onBankTransferPayment(BankTransferPaymentEvent paymentEvent,
                                      @Header(value = AppConstants.TRACE_ID_HEADER, required = false) String traceId) {

        // Set trace-id in MDC (generate if not present)
        if (traceId == null || traceId.trim().isEmpty()) {
            traceId = UUID.randomUUID().toString();
            log.debug("Generated new trace-id for Kafka message: {}", traceId);
        }
        try {
            MDC.put(AppConstants.TRACE_ID_MDC_KEY, traceId);
            log.info("Received bank transfer payment event: {}", paymentEvent);
            String txDesc = paymentEvent.getTransactionDescription();

            // Format: <10-char E2E id> <8-char reservationId> e.g. "1401541457 P4145478"
            String[] parts = txDesc.trim().split("\\s+");
            if (parts.length != AppConstants.EXPECTED_TRANSACTION_PARTS
                    || parts[0].length() != AppConstants.E2E_ID_LENGTH
                    || parts[1].length() != AppConstants.RESERVATION_ID_LENGTH) {
                log.warn("Invalid transactionDescription format: {}", txDesc);
                return;
            }
            String reservationPublicId = parts[1];

            // ReservationPublicId is numeric id string "00000001".
            Long reservationId = parseReservationId(reservationPublicId);
            BigDecimal amountReceived = paymentEvent.getAmountReceived();

            log.info("Processing bank transfer payment for reservation ID: {}, amount: {}", reservationId, amountReceived);
            reservationService.confirmBankTransferPayment(reservationId, amountReceived);

            log.info("Successfully processed bank transfer payment for reservation ID: {}", reservationId);
        } catch (Exception ex) {
            log.error("Error processing bank transfer event for trace-id: {}", traceId, ex);
        } finally {
            // Clean up MDC
            MDC.clear();
        }
    }

    private Long parseReservationId(String reservationPublicId) {
        return Long.parseLong(reservationPublicId.replaceAll("\\D", ""));
    }
}

