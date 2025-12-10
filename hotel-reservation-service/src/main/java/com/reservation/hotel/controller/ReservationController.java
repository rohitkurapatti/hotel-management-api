package com.reservation.hotel.controller;

import com.reservation.hotel.model.ReservationRequest;
import com.reservation.hotel.model.ReservationResponse;
import com.reservation.hotel.services.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@Slf4j
@Tag(name = "Reservation Management", description = "APIs for managing hotel room reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService){
        this.reservationService = reservationService;
    }

    @PostMapping("/confirm")
    @Operation(
            summary = "Create a new hotel reservation",
            description = "Creates a new reservation for a hotel room. Supports multiple payment modes: CASH (immediate confirmation), CREDIT_CARD (validates with payment service), and BANK_TRANSFER (pending payment status)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Reservation created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input - validation errors or payment issues",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Payment reference not found (for credit card payments)",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationRequest reservationRequest){

        log.info("Creating reservation for customer: {} ", reservationRequest.getCustomerName());
        ReservationResponse reservationResponse = reservationService.confirmReservation(reservationRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reservationResponse);
    }

}
