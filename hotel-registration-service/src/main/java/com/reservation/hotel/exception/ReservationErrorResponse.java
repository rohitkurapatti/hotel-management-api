package com.reservation.hotel.exception;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
public class ReservationErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String path;
    private String traceId;
    private Map<String, String> errors;

    public ReservationErrorResponse(int status, String error, String path, String traceId, Map<String, String> errors) {
        this.timestamp = timestamp.now();
        this.status = status;
        this.error = error;
        this.path = path;
        this.traceId = traceId;
        this.errors = errors;
    }
}
