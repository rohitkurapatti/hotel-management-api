package com.hotel.creditcard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.payment.creditcard.api")
public class CreditCardClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(CreditCardClientApplication.class, args);
    }
}
