package com.hotel.creditcard.mocks;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentStatusRepository extends JpaRepository<PaymentStatus, String> {
}
