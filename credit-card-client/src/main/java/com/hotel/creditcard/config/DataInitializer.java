package com.hotel.creditcard.config;

import com.hotel.creditcard.mocks.PaymentStatus;
import com.hotel.creditcard.mocks.PaymentStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PaymentStatusRepository paymentStatusRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing payment status reference data...");

        // Check if data already exists to avoid duplicates on restart
        if (paymentStatusRepository.count() == 0) {
            // Payment status reference data
            paymentStatusRepository.save(new PaymentStatus("PP111111", "CONFIRMED"));
            paymentStatusRepository.save(new PaymentStatus("PR222222", "CANCELLED"));
            paymentStatusRepository.save(new PaymentStatus("PN333333", "PENDING"));

            log.info("Successfully initialized {} payment status records", paymentStatusRepository.count());
        } else {
            log.info("Payment status data already exists. Skipping initialization.");
        }
    }
}

