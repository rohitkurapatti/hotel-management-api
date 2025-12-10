package com.reservation.hotel.services;


import com.reservation.hotel.dto.ReservationRequest;
import com.reservation.hotel.dto.ReservationResponse;

import java.math.BigDecimal;

public interface ReservationService {

    ReservationResponse confirmReservation(ReservationRequest reservationRequest);

    void confirmBankTransferPayment(Long reservationId, BigDecimal amountReceived);

    int cancelPendingBankTransferReservations();

}
