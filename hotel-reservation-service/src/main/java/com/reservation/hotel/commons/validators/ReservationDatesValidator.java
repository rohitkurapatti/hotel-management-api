package com.reservation.hotel.commons.validators;

import com.reservation.hotel.model.ReservationRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ReservationDatesValidator implements ConstraintValidator<ValidReservationDates, ReservationRequest> {

    @Override
    public boolean isValid(ReservationRequest reservationRequest, ConstraintValidatorContext validatorContext) {

        if(reservationRequest == null) return true;

        LocalDate start = reservationRequest.getStartDate();
        LocalDate end = reservationRequest.getEndDate();

        // If either date is null, let @NotNull handle it
        if (start == null || end == null) {
            return true;
        }

        validatorContext.disableDefaultConstraintViolation();

        // endDate must be after startDate
        if (!end.isAfter(start)) {
            validatorContext.buildConstraintViolationWithTemplate("End date must be after start date")
                    .addPropertyNode("endDate")
                    .addConstraintViolation();
            return false;
        }

        // Cannot reserve in the past
        if (start.isBefore(LocalDate.now())) {
            validatorContext.buildConstraintViolationWithTemplate("Start date cannot be in the past")
                    .addPropertyNode("startDate")
                    .addConstraintViolation();
            return false;
        }

        // Cannot reserve more than 30 days (as per assignment)
        long days = ChronoUnit.DAYS.between(start, end);
        if (days > 30) {
            validatorContext.buildConstraintViolationWithTemplate("Reservation cannot exceed 30 days")
                    .addPropertyNode("endDate")
                    .addConstraintViolation();
            return false;
        }

        return true;

    }
}
