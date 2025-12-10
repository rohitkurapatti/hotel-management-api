package com.reservation.hotel.model;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public enum RoomSegment {
    SMALL(new BigDecimal("1200")),
    MEDIUM(new BigDecimal("2000")),
    LARGE(new BigDecimal("3200")),
    EXTRA_LARGE(new BigDecimal("4500"));

    private final BigDecimal pricePerDay;

    RoomSegment(BigDecimal pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

}
