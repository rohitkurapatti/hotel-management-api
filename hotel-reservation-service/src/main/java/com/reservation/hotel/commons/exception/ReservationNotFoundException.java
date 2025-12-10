package com.reservation.hotel.commons.exception;

import org.jetbrains.annotations.NotNull;

public class ReservationNotFoundException extends RuntimeException
{
    public ReservationNotFoundException(String message) {
        super(message);
    }
}
