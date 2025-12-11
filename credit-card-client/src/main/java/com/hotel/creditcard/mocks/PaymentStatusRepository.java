package com.hotel.creditcard.mocks;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;

@ConditionalOnProperty(
    value = "mock.credit-card-service.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public interface PaymentStatusRepository extends JpaRepository<PaymentStatus, String> {
}
