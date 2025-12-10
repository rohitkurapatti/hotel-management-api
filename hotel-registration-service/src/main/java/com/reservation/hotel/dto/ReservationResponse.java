package com.reservation.hotel.dto;

import com.reservation.hotel.model.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing reservation confirmation details")
public class ReservationResponse {

    @Schema(description = "Unique identifier for the reservation", example = "1")
    private Long reservationId;

    @Schema(description = "Current status of the reservation", example = "CONFIRMED", allowableValues = {"CONFIRMED", "PENDING_PAYMENT", "CANCELLED"})
    private ReservationStatus reservationStatus;


}
