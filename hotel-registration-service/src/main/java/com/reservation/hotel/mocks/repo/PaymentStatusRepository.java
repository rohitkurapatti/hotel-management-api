package com.reservation.hotel.mocks.repo;

import com.reservation.hotel.mocks.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentStatusRepository extends JpaRepository<PaymentStatus, String> {
}
