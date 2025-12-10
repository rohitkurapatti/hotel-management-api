package com.reservation.hotel.commons.exception;

public class PaymentReferenceNotFoundException extends RuntimeException{

    public PaymentReferenceNotFoundException(String message) {
        super(message);
    }

}
