package com.hotel.creditcard.mocks;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Entity
@Table(name = "payment_status")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@ConditionalOnProperty(name = "mock.credit-card-service.enabled", havingValue = "true")
public class PaymentStatus {

    @Id
    private String paymentReference;

    /**
     * Possible values: CONFIRMED, REJECTED, PENDING
     */
    private String status;
}
