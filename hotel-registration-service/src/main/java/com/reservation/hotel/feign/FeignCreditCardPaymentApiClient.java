package com.reservation.hotel.feign;

import com.payment.creditcard.model.PaymentStatusResponse;
import com.payment.creditcard.model.PaymentStatusRetrievalRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "credit-card-payment-service",
        url = "${services.creditcard.base-url}"
)
public interface FeignCreditCardPaymentApiClient {

    @PostMapping("/payment-status")
    PaymentStatusResponse paymentStatusPost(@RequestBody PaymentStatusRetrievalRequest request);

}
