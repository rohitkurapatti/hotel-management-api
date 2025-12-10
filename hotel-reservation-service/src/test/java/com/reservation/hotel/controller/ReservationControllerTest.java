package com.reservation.hotel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservation.hotel.model.ReservationRequest;
import com.reservation.hotel.model.ReservationResponse;
import com.reservation.hotel.commons.exception.GlobalExceptionHandler;
import com.reservation.hotel.model.PaymentMode;
import com.reservation.hotel.model.ReservationStatus;
import com.reservation.hotel.model.RoomSegment;
import com.reservation.hotel.services.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ReservationControllerTest {

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private ReservationController reservationController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(reservationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }


//    Success scenario
    @Test
    void createReservationWithValidRequestShouldReturnCreated() throws Exception {
        ReservationRequest request = createValidReservationRequest();
        performSuccessfulReservationRequest(request, 1L, ReservationStatus.CONFIRMED);
    }

    @Test
    void createReservationWithBankTransferPaymentShouldReturnPendingPayment() throws Exception {
        ReservationRequest request = createValidReservationRequest();
        request.setPaymentMode(PaymentMode.BANK_TRANSFER);
        request.setPaymentReference("BT123456");
        performSuccessfulReservationRequest(request, 2L, ReservationStatus.PENDING_PAYMENT);
    }

//  Validation
    @Test
    void createReservationWithNullRequiredFieldsShouldReturnBadRequest() throws Exception {
        ReservationRequest request = createValidReservationRequest();
        request.setCustomerName(null);
        performInvalidReservationRequest(request);
    }

    @Test
    void createReservationWithInvalidPaymentReferenceShouldReturnBadRequest() throws Exception {
        ReservationRequest request = createValidReservationRequest();
        // lowercase - violates pattern
        request.setPaymentReference("invalid123");
        performInvalidReservationRequest(request);
    }

    @Test
    void createReservationWithInvalidDateRangeShouldReturnBadRequest() throws Exception {
        ReservationRequest request = createValidReservationRequest();
        request.setStartDate(LocalDate.now().plusDays(5));
        // End before start
        request.setEndDate(LocalDate.now().plusDays(2));
        performInvalidReservationRequest(request);
    }


//  Error scenario
    @Test
    void createReservationWithMalformedJsonShouldReturnBadRequest() throws Exception {
        performInvalidReservationRequestWithContent("{ invalid json }");
    }

    private ReservationRequest createValidReservationRequest() {
        ReservationRequest request = new ReservationRequest();
        request.setCustomerName("John Doe");
        request.setRoomNumber("101");
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));
        request.setRoomSegment(RoomSegment.MEDIUM);
        request.setPaymentMode(PaymentMode.CASH);
        request.setPaymentReference("PR123456");
        return request;
    }

    private void performSuccessfulReservationRequest(ReservationRequest request, Long expectedId, ReservationStatus expectedStatus) throws Exception {
        when(reservationService.confirmReservation(any(ReservationRequest.class)))
                .thenReturn(new ReservationResponse(expectedId, expectedStatus));

        mockMvc.perform(post("/api/reservations/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reservationId").value(expectedId))
                .andExpect(jsonPath("$.reservationStatus").value(expectedStatus.name()));

        verify(reservationService, times(1)).confirmReservation(any(ReservationRequest.class));
    }

    private void performInvalidReservationRequest(ReservationRequest request) throws Exception {
        mockMvc.perform(post("/api/reservations/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(reservationService, never()).confirmReservation(any(ReservationRequest.class));
    }

    private void performInvalidReservationRequestWithContent(String content) throws Exception {
        mockMvc.perform(post("/api/reservations/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest());

        verify(reservationService, never()).confirmReservation(any(ReservationRequest.class));
    }
}

