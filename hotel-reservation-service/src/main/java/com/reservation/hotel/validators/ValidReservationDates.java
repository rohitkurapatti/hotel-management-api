package com.reservation.hotel.validators;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ReservationDatesValidator.class)
@Documented
public @interface ValidReservationDates {

    String message() default "Invalid reservation dates";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
