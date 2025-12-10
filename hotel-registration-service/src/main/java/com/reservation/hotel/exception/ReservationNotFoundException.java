package com.reservation.hotel.exception;

import org.jetbrains.annotations.NotNull;

public class ReservationNotFoundException extends RuntimeException
{
    public ReservationNotFoundException(String message) {
        super(message);
    }
}
