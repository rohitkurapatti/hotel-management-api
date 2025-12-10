package com.reservation.hotel.exception;

public class PaymentNotConfirmedException extends RuntimeException {

    public PaymentNotConfirmedException(String message) {
        super(message);
    }

}
