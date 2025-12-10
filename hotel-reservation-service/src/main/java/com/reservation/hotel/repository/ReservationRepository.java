package com.reservation.hotel.repository;

import com.reservation.hotel.model.PaymentMode;
import com.reservation.hotel.entities.Reservation;
import com.reservation.hotel.model.RoomSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // existing reservation
    Optional<Reservation> findFirstByCustomerNameAndRoomNumberAndStartDateAndEndDateAndRoomSegmentAndPaymentMode(
            String customerName,
            String roomNumber,
            LocalDate startDate,
            LocalDate endDate,
            RoomSegment roomSegment,
            PaymentMode paymentMode
    );

    //Update in bulk to make status CANCELLED for pending bank transfer payments before 2 days of start date
    @Modifying
    @Query("UPDATE Reservation r SET r.reservationStatus = 'CANCELLED' " +
           "WHERE r.paymentMode = 'BANK_TRANSFER' " +
           "AND r.reservationStatus = 'PENDING_PAYMENT' " +
           "AND r.startDate <= :cutoffDate")
    int cancelPendingBankTransfers(@Param("cutoffDate") LocalDate cutoffDate);

    // To know the IDs of the pending payment reservations before 2 days
    @Query("SELECT r.reservationId FROM Reservation r " +
           "WHERE r.paymentMode = 'BANK_TRANSFER' " +
           "AND r.reservationStatus = 'PENDING_PAYMENT' " +
           "AND r.startDate <= :cutoffDate")
    List<Long> findPendingBankTransferIds(@Param("cutoffDate") LocalDate cutoffDate);

}
