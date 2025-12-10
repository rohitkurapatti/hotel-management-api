package com.reservation.hotel.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String TRACE_ID_MDC_KEY = "traceId";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ReservationErrorResponse> validationHandler(MethodArgumentNotValidException ex,
                                                                      HttpServletRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }

        ReservationErrorResponse apiError = new ReservationErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                request.getRequestURI(),
                MDC.get(TRACE_ID_MDC_KEY),
                fieldErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ReservationErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();

        ex.getConstraintViolations().forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            // Extract just the field name (e.g., "getReservation.id" -> "id")
            String fieldName = propertyPath.contains(".")
                ? propertyPath.substring(propertyPath.lastIndexOf('.') + 1)
                : propertyPath;
            errors.put(fieldName, violation.getMessage());
        });

        ReservationErrorResponse errorResponse = new ReservationErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                request.getRequestURI(),
                MDC.get(TRACE_ID_MDC_KEY),
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ReservationErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        Map<String, String> errorResponse = new HashMap<>();
        String errorType = "Invalid Request Format";

        // Try to extract the root cause for detailed error information
        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException ife) {
            String fieldName = !ife.getPath().isEmpty() ? ife.getPath().get(0).getFieldName() : "unknown";

            if (ife.getTargetType().equals(java.time.LocalDate.class)) {
                String providedValue = ife.getValue() != null ? ife.getValue().toString() : "null";
                errorResponse.put(fieldName,
                    String.format("Invalid date format '%s'. Expected format: yyyy-MM-dd (e.g., 2025-12-31)", providedValue));
            } else if (ife.getTargetType().isEnum()) {
                String allowedValues = Arrays.toString(ife.getTargetType().getEnumConstants());
                errorResponse.put(fieldName, "Invalid value. Allowed values: " + allowedValues);
            } else {
                String providedValue = ife.getValue() != null ? ife.getValue().toString() : "null";
                errorResponse.put(fieldName,
                    String.format("Invalid value '%s' for type %s", providedValue, ife.getTargetType().getSimpleName()));
            }
        } else {
            // For other JSON parsing errors, provide a descriptive message
            String message = ex.getMostSpecificCause().getMessage();
            if (message != null && message.length() > 200) {
                message = message.substring(0, 200) + "...";
            }
            errorResponse.put("request", message != null ? message : "Malformed JSON request");
        }

        ReservationErrorResponse apiError = new ReservationErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                errorType,
                request.getRequestURI(),
                MDC.get(TRACE_ID_MDC_KEY),
                errorResponse
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<ReservationErrorResponse> handleInvalidFormat(InvalidFormatException ex, HttpServletRequest request) {

        String fieldName = !ex.getPath().isEmpty() ? ex.getPath().get(0).getFieldName() : "unknown";
        Map<String, String> errorResponse = new HashMap<>();

        if (ex.getTargetType().isEnum()) {
            String allowedValues = Arrays.toString(ex.getTargetType().getEnumConstants());
            errorResponse.put(fieldName, "Invalid value. Allowed values: " + allowedValues);
        } else if (ex.getTargetType().equals(java.time.LocalDate.class)) {
            String providedValue = ex.getValue() != null ? ex.getValue().toString() : "null";
            errorResponse.put(fieldName,
                String.format("Invalid date format '%s'. Expected format: yyyy-MM-dd (e.g., 2025-12-31)", providedValue));
        } else {
            errorResponse.put(fieldName, "Invalid format for field");
        }

        ReservationErrorResponse apiError = new ReservationErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Format",
                request.getRequestURI(),
                MDC.get(TRACE_ID_MDC_KEY),
                errorResponse
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);


    }

    @ExceptionHandler(PaymentNotConfirmedException.class)
    public ResponseEntity<ReservationErrorResponse> handlePaymentFailure(PaymentNotConfirmedException ex, HttpServletRequest request) {

        ReservationErrorResponse apiError = new ReservationErrorResponse(
                HttpStatus.PAYMENT_REQUIRED.value(),
                "Payment Not Confirmed",
                request.getRequestURI(),
                MDC.get(TRACE_ID_MDC_KEY),
                Map.of("paymentStatus", ex.getMessage())
        );

        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(apiError);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ReservationErrorResponse> handleBadRequest(IllegalArgumentException ex, HttpServletRequest request) {

        ReservationErrorResponse apiError = new ReservationErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                request.getRequestURI(),
                MDC.get(TRACE_ID_MDC_KEY),
                Map.of("error", ex.getMessage())
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(PaymentReferenceNotFoundException.class)
    public ResponseEntity<ReservationErrorResponse> handlePaymentReferenceNotFound(
            PaymentReferenceNotFoundException ex,
            HttpServletRequest request) {

        ReservationErrorResponse response = new ReservationErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Payment Reference Not Found",
                request.getRequestURI(),
                MDC.get("traceId"),
                Map.of("paymentReference", ex.getMessage())
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ReservationErrorResponse> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request) {
        String traceId = MDC.get(TRACE_ID_MDC_KEY);
        log.error("[TraceId: {}] Bad credentials from IP: {}", traceId, request.getRemoteAddr());

        ReservationErrorResponse response = new ReservationErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Authentication Failed",
                request.getRequestURI(),
                traceId,
                Map.of("error", "Invalid username or password")
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ReservationErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request) {
        String traceId = MDC.get(TRACE_ID_MDC_KEY);
        log.error("[TraceId: {}] Authentication error: {} from IP: {}", traceId, ex.getMessage(), request.getRemoteAddr());

        ReservationErrorResponse response = new ReservationErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Authentication Failed",
                request.getRequestURI(),
                traceId,
                Map.of("error", "Authentication failed")
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ReservationErrorResponse> handleOthers(Exception ex, HttpServletRequest request) {
        String traceId = MDC.get(TRACE_ID_MDC_KEY);
        log.error("[TraceId: {}] Unexpected error: {}", traceId, ex.getMessage(), ex);

        ReservationErrorResponse apiError = new ReservationErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                request.getRequestURI(),
                traceId,
                Map.of("error", "Unexpected failure occurred")
        );


        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }

}
