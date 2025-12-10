package com.reservation.hotel.mocks;

import com.payment.creditcard.api.PaymentStatusApi;
import com.payment.creditcard.model.ErrorResponse;
import com.payment.creditcard.model.PaymentStatusResponse;
import com.payment.creditcard.model.PaymentStatusRetrievalRequest;
import com.reservation.hotel.mocks.repo.PaymentStatusRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/mock-credit-card")
public class MockPaymentStatusController implements PaymentStatusApi {

    private final PaymentStatusRepository paymentStatusRepository;

    public MockPaymentStatusController(PaymentStatusRepository paymentStatusRepository) {
        this.paymentStatusRepository = paymentStatusRepository;
    }

    @Override
    public ResponseEntity<PaymentStatusResponse> paymentStatusPost(PaymentStatusRetrievalRequest request) {
        log.info("Mock Payment Service received reference: {}", request.getPaymentReference());

        // Validate input - return HTTP 400
        if (request.getPaymentReference() == null || request.getPaymentReference().isBlank()) {
            ErrorResponse error = new ErrorResponse();
            error.setError("Invalid payment reference");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }

        PaymentStatus record = paymentStatusRepository
                .findById(request.getPaymentReference()).orElse(null);

        if (record == null) {
            ErrorResponse err = new ErrorResponse();
            err.setError("Payment not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }

        // Build successful response
        PaymentStatusResponse response = new PaymentStatusResponse();
        response.setStatus(record.getStatus());
        response.setLastUpdateDate(LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }

}

