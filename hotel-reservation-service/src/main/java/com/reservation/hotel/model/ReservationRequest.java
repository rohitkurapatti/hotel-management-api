package com.reservation.hotel.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.reservation.hotel.commons.validators.ValidReservationDates;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@ValidReservationDates
@Schema(description = "Request object for creating a hotel room reservation")
public class ReservationRequest {

    @NotBlank(message = "Customer name should not be empty")
    @Schema(description = "Full name of the customer", example = "John Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String customerName;

    @NotBlank(message = "Room number should not be empty")
    @Schema(description = "Room number to be reserved", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
    private String roomNumber;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "Check-in date", example = "2025-12-15", requiredMode = Schema.RequiredMode.REQUIRED, type = "string", format = "date")
    private LocalDate startDate;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "Check-out date (must be after start date)", example = "2025-12-18", requiredMode = Schema.RequiredMode.REQUIRED, type = "string", format = "date")
    private LocalDate endDate;

    @NotNull
    @Schema(description = "Room segment/category", example = "MEDIUM", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"SMALL", "MEDIUM", "LARGE", "EXTRA_LARGE"})
    private RoomSegment roomSegment;

    @NotNull
    @Schema(description = "Payment method", example = "CASH", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"CASH", "CREDIT_CARD", "BANK_TRANSFER"})
    private PaymentMode paymentMode;

    @Pattern(
            regexp = "^[A-Z]{2}\\d+$",
            message = "paymentReference must have first 2 uppercase letters followed by digits (e.g., PR123456)"
    )
    @Schema(description = "Payment reference ID (2 uppercase letters followed by digits)", example = "PR123456", pattern = "^[A-Z]{2}\\d+$")
    private String paymentReference;



}
