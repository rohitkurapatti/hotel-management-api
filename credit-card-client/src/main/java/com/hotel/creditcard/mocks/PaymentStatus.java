package com.hotel.creditcard.mocks;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "payment_status")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PaymentStatus {

    @Id
    private String paymentReference;

    /**
     * Possible values: CONFIRMED, REJECTED, PENDING
     */
    private String status;
}
