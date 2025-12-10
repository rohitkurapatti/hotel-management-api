package com.reservation.hotel.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankTransferPaymentEvent {

    private String paymentId;
    private String debtorAccountNumber;
    private BigDecimal amountReceived;
    private String transactionDescription;
}
