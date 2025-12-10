package com.reservation.hotel.entities;

import com.reservation.hotel.model.PaymentMode;
import com.reservation.hotel.model.ReservationStatus;
import com.reservation.hotel.model.RoomSegment;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long reservationId;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String roomNumber;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomSegment roomSegment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMode paymentMode;

    private String paymentReference;

    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus;

    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
