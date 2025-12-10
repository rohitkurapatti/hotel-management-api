package com.reservation.hotel.config.kafka;

import com.reservation.hotel.model.BankTransferPaymentEvent;
import com.reservation.hotel.services.ReservationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankTransferPaymentListenerTest {

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private BankTransferPaymentListener listener;

    @AfterEach
    void cleanup() {
        MDC.clear();
    }

    @Test
    void testOnBankTransferPaymentWithValidEventAndTraceIdShouldProcessSuccessfully() {
        // Arrange
        String traceId = "test-trace-123";
        BankTransferPaymentEvent event = new BankTransferPaymentEvent();
        event.setPaymentId("PAY123456");
        event.setDebtorAccountNumber("1234567890");
        event.setAmountReceived(new BigDecimal("2000.00"));
        event.setTransactionDescription("1401541457 P0000001");
        listener.onBankTransferPayment(event, traceId);

        verify(reservationService, times(1))
                .confirmBankTransferPayment(1L, new BigDecimal("2000.00"));
        assertNull(MDC.get("traceId"));
    }

    @Test
    void testOnBankTransferPaymentWithInvalidDescriptionFormatShouldNotProcessPayment() {
        // Arrange
        String traceId = "test-trace-456";
        BankTransferPaymentEvent event = new BankTransferPaymentEvent();
        event.setPaymentId("PAY123456");
        event.setDebtorAccountNumber("1234567890");
        event.setAmountReceived(new BigDecimal("2000.00"));
        event.setTransactionDescription("INVALID FORMAT");

        listener.onBankTransferPayment(event, traceId);
        verify(reservationService, never()).confirmBankTransferPayment(anyLong(), any(BigDecimal.class));
        assertNull(MDC.get("traceId"));
    }
}

