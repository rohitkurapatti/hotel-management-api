package com.reservation.hotel.exception;

public class PaymentReferenceNotFoundException extends RuntimeException{

    public PaymentReferenceNotFoundException(String message) {
        super(message);
    }

}
