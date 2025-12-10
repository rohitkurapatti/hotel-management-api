package com.reservation.hotel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.reservation.hotel.feign")
@EnableScheduling
public class HotelReservationApplication {
    public static void main(String[] args) {
        SpringApplication.run(HotelReservationApplication.class, args);
    }
}
