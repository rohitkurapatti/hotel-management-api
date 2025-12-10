package com.reservation.hotel.validators;

import com.reservation.hotel.dto.ReservationRequest;
import com.reservation.hotel.model.PaymentMode;
import com.reservation.hotel.model.RoomSegment;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReservationDatesValidator Tests")
class ReservationDatesValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should pass validation with valid reservation dates")
    void testValidReservationDates() {
        ReservationRequest request = createValidReservationRequest();
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(5));

        Set<ConstraintViolation<ReservationRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty(), "Valid dates should not produce violations");
    }

    @Test
    @DisplayName("Should fail validation when end date is before start date")
    void testEndDateBeforeStartDate() {

        ReservationRequest request = createValidReservationRequest();
        request.setStartDate(LocalDate.now().plusDays(5));
        request.setEndDate(LocalDate.now().plusDays(3));
        Set<ConstraintViolation<ReservationRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty(), "End date before start date should produce violation");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("End date must be after start date")),
                "Should have specific error message for end date before start date");
    }

    @Test
    @DisplayName("Should fail validation when reservation exceeds 30 days")
    void testReservationExceeds30Days() {
        ReservationRequest request = createValidReservationRequest();
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(32)); // 31 days duration

        Set<ConstraintViolation<ReservationRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty(), "Reservation exceeding 30 days should produce violation");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Reservation cannot exceed 30 days")),
                "Should have specific error message for exceeding 30 days");
    }

    // Helper method
    private ReservationRequest createValidReservationRequest() {
        ReservationRequest request = new ReservationRequest();
        request.setCustomerName("John Doe");
        request.setRoomNumber("101");
        request.setRoomSegment(RoomSegment.MEDIUM);
        request.setPaymentMode(PaymentMode.CASH);
        request.setPaymentReference("PR123456");
        return request;
    }
}

