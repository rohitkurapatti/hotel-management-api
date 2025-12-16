package com.reservation.hotel.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.creditcard.api.PaymentStatusApiClient;
import com.payment.creditcard.model.PaymentStatusResponse;
import com.payment.creditcard.model.PaymentStatusRetrievalRequest;
import com.reservation.hotel.commons.exception.ServiceUnavailableException;
import com.reservation.hotel.model.ReservationRequest;
import com.reservation.hotel.model.ReservationResponse;
import com.reservation.hotel.entities.Reservation;
import com.reservation.hotel.commons.exception.PaymentNotConfirmedException;
import com.reservation.hotel.commons.exception.PaymentReferenceNotFoundException;
import com.reservation.hotel.commons.exception.ReservationNotFoundException;
import com.reservation.hotel.model.*;
import com.reservation.hotel.repository.ReservationRepository;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private PaymentStatusApiClient paymentStatusApiClient;

    private ObjectMapper objectMapper;

    private ReservationServiceImpl reservationService;

    private ReservationRequest reservationRequest;
    private Reservation reservation;

    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // for LocalDate support

        // instantiate the service
        reservationService = new ReservationServiceImpl(reservationRepository, paymentStatusApiClient, objectMapper);

        reservationRequest = new ReservationRequest();
        reservationRequest.setCustomerName("John Doe");
        reservationRequest.setRoomNumber("101");
        reservationRequest.setStartDate(LocalDate.now().plusDays(5));
        reservationRequest.setEndDate(LocalDate.now().plusDays(7));
        reservationRequest.setRoomSegment(RoomSegment.MEDIUM);
        reservationRequest.setPaymentMode(PaymentMode.CASH);

        reservation = new Reservation();
        reservation.setReservationId(1L);
        reservation.setCustomerName("John Doe");
        reservation.setRoomNumber("101");
        reservation.setStartDate(LocalDate.now().plusDays(5));
        reservation.setEndDate(LocalDate.now().plusDays(7));
        reservation.setRoomSegment(RoomSegment.MEDIUM);
        reservation.setPaymentMode(PaymentMode.CASH);
        reservation.setTotalAmount(new BigDecimal("2000"));
        reservation.setReservationStatus(ReservationStatus.CONFIRMED);
    }

    // Helper methods
    private void mockNoExistingReservation() {
        when(reservationRepository.findFirstByCustomerNameAndRoomNumberAndStartDateAndEndDateAndRoomSegmentAndPaymentMode(
                anyString(), anyString(), any(LocalDate.class), any(LocalDate.class),
                any(RoomSegment.class), any(PaymentMode.class)))
                .thenReturn(Optional.empty());
    }


    private void mockReservationSave() {
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);
    }

    private void setupCreditCardTest(String paymentReference) {
        reservationRequest.setPaymentMode(PaymentMode.CREDIT_CARD);
        reservationRequest.setPaymentReference(paymentReference);
        reservation.setPaymentMode(PaymentMode.CREDIT_CARD);
        mockNoExistingReservation();
    }

    private PaymentStatusResponse createPaymentStatusResponse(String status) {
        PaymentStatusResponse response = new PaymentStatusResponse();
        response.setStatus(status);
        return response;
    }

    private void setupBankTransferReservation(Long reservationId, BigDecimal totalAmount, ReservationStatus status) {
        reservation.setReservationId(reservationId);
        reservation.setPaymentMode(PaymentMode.BANK_TRANSFER);
        reservation.setReservationStatus(status);
        reservation.setTotalAmount(totalAmount);
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
    }

    @Test
    void testConfirmReservationWithCashPaymentShouldReturnConfirmedStatus() {
        reservationRequest.setPaymentMode(PaymentMode.CASH);
        mockNoExistingReservation();
        mockReservationSave();

        ReservationResponse response = reservationService.confirmReservation(reservationRequest);
        assertNotNull(response);
        assertEquals(1L, response.getReservationId());
        assertEquals(ReservationStatus.CONFIRMED, response.getReservationStatus());
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void testConfirmReservationWithExistingReservationShouldReturnExistingReservation() {
        
        when(reservationRepository.findFirstByCustomerNameAndRoomNumberAndStartDateAndEndDateAndRoomSegmentAndPaymentMode(
                anyString(), anyString(), any(LocalDate.class), any(LocalDate.class),
                any(RoomSegment.class), any(PaymentMode.class)))
                .thenReturn(Optional.of(reservation));
        ReservationResponse response = reservationService.confirmReservation(reservationRequest);
        assertNotNull(response);
        assertEquals(1L, response.getReservationId());
        assertEquals(ReservationStatus.CONFIRMED, response.getReservationStatus());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void testConfirmReservationWithCreditCardConfirmedShouldReturnConfirmedStatus() {
        setupCreditCardTest("CC123456");
        when(paymentStatusApiClient.paymentStatusPost(any(PaymentStatusRetrievalRequest.class)))
                .thenReturn(ResponseEntity.ok(createPaymentStatusResponse("CONFIRMED")));
        mockReservationSave();
        ReservationResponse response = reservationService.confirmReservation(reservationRequest);
        assertNotNull(response);
        assertEquals(ReservationStatus.CONFIRMED, response.getReservationStatus());
        verify(paymentStatusApiClient).paymentStatusPost(any(PaymentStatusRetrievalRequest.class));
    }

    @Test
    void testConfirmReservationWithCreditCardCancelledShouldThrowException() {
        setupCreditCardTest("CC123456");
        when(paymentStatusApiClient.paymentStatusPost(any(PaymentStatusRetrievalRequest.class)))
                .thenReturn(ResponseEntity.ok(createPaymentStatusResponse("CANCELLED")));

        assertThrows(PaymentNotConfirmedException.class,
                () -> reservationService.confirmReservation(reservationRequest));
    }

    @Test
    void testConfirmReservationWithCreditCardNotFoundShouldThrowException() {
        setupCreditCardTest("CC123456");
        when(paymentStatusApiClient.paymentStatusPost(any(PaymentStatusRetrievalRequest.class)))
                .thenThrow(createFeignNotFoundException());
        assertThrows(PaymentReferenceNotFoundException.class,
                () -> reservationService.confirmReservation(reservationRequest));
    }

    @Test
    void testConfirmReservationWithCreditCardPendingStatus() {
        setupCreditCardTest("CC123456");
        when(paymentStatusApiClient.paymentStatusPost(any(PaymentStatusRetrievalRequest.class)))
                .thenReturn(ResponseEntity.ok(createPaymentStatusResponse("PENDING")));

        assertThrows(PaymentNotConfirmedException.class,
                () -> reservationService.confirmReservation(reservationRequest));
    }

    @Test
    void testConfirmReservationWithCreditCardBadRequest() {
        setupCreditCardTest("CC123456");
        when(paymentStatusApiClient.paymentStatusPost(any(PaymentStatusRetrievalRequest.class)))
                .thenThrow(createFeignBadRequestException());

        assertThrows(PaymentReferenceNotFoundException.class,
                () -> reservationService.confirmReservation(reservationRequest));
    }

    @Test
    void testConfirmReservationWithCreditCardInternalServerError() {
        setupCreditCardTest("CC123456");
        when(paymentStatusApiClient.paymentStatusPost(any(PaymentStatusRetrievalRequest.class)))
                .thenThrow(createFeignInternalServerException());

        assertThrows(ServiceUnavailableException.class,
                () -> reservationService.confirmReservation(reservationRequest));
    }

    @Test
    void testConfirmReservationWithCreditCardGenericFeignException() {
        setupCreditCardTest("CC123456");
        when(paymentStatusApiClient.paymentStatusPost(any(PaymentStatusRetrievalRequest.class)))
                .thenThrow(createFeignServiceUnavailableException());

        assertThrows(ServiceUnavailableException.class,
                () -> reservationService.confirmReservation(reservationRequest));
    }

    @Test
    void testConfirmReservationWithCreditCardNullResponse() {
        setupCreditCardTest("CC123456");
        when(paymentStatusApiClient.paymentStatusPost(any(PaymentStatusRetrievalRequest.class)))
                .thenReturn(ResponseEntity.ok(null));

        assertThrows(PaymentNotConfirmedException.class,
                () -> reservationService.confirmReservation(reservationRequest));
    }

    @Test
    void testConfirmReservationWithBankTransferShouldReturnPendingStatus() {
        reservationRequest.setPaymentMode(PaymentMode.BANK_TRANSFER);
        reservation.setPaymentMode(PaymentMode.BANK_TRANSFER);
        reservation.setReservationStatus(ReservationStatus.PENDING_PAYMENT);
        mockNoExistingReservation();
        mockReservationSave();
        ReservationResponse response = reservationService.confirmReservation(reservationRequest);
        assertNotNull(response);
        assertEquals(ReservationStatus.PENDING_PAYMENT, response.getReservationStatus());
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void testConfirmBankTransferPaymentFullPaymentShouldConfirmReservation() {
        Long reservationId = 1L;
        BigDecimal expectedAmount = new BigDecimal("2000");
        BigDecimal receivedAmount = new BigDecimal("2000");
        setupBankTransferReservation(reservationId, expectedAmount, ReservationStatus.PENDING_PAYMENT);
        mockReservationSave();
        reservationService.confirmBankTransferPayment(reservationId, receivedAmount);
        verify(reservationRepository).save(argThat(res ->
            res.getReservationStatus() == ReservationStatus.CONFIRMED
        ));
    }

    @Test
    void testConfirmBankTransferPaymentNotBankTransferModeShouldLogErrorAndReturn() {
        Long reservationId = 1L;
        BigDecimal receivedAmount = new BigDecimal("2000");
        reservation.setPaymentMode(PaymentMode.CASH);
        reservation.setReservationStatus(ReservationStatus.CONFIRMED);
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        
        // Should not throw exception, just log error and return
        assertDoesNotThrow(() -> reservationService.confirmBankTransferPayment(reservationId, receivedAmount));
        
        // Verify that save was never called (reservation should not be modified)
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void testConfirmBankTransferPaymentReservationNotFoundShouldLogErrorAndReturn() {
        Long reservationId = 999L;
        BigDecimal receivedAmount = new BigDecimal("2000");
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());
        
        // Should not throw exception, just log error and return
        assertDoesNotThrow(() -> reservationService.confirmBankTransferPayment(reservationId, receivedAmount));
        
        // Verify that save was never called
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void testConfirmBankTransferPaymentCancelledReservationShouldLogWarningAndReturn() {
        Long reservationId = 1L;
        BigDecimal receivedAmount = new BigDecimal("2000");
        setupBankTransferReservation(reservationId, new BigDecimal("2000"), ReservationStatus.CANCELLED);
        
        // Should not throw exception, just log warning and return
        assertDoesNotThrow(() -> reservationService.confirmBankTransferPayment(reservationId, receivedAmount));
        
        // Verify that save was never called (cancelled reservation should not be modified)
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void testConfirmBankTransferPaymentAlreadyConfirmedShouldBeIdempotent() {
        Long reservationId = 1L;
        BigDecimal receivedAmount = new BigDecimal("2000");
        setupBankTransferReservation(reservationId, new BigDecimal("2000"), ReservationStatus.CONFIRMED);
        
        // Should not throw exception, just log info and return (idempotent)
        assertDoesNotThrow(() -> reservationService.confirmBankTransferPayment(reservationId, receivedAmount));
        
        // Verify that save was never called (already confirmed)
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void testConfirmBankTransferPaymentPartialPaymentShouldLogWarningAndNotConfirm() {
        Long reservationId = 1L;
        BigDecimal expectedAmount = new BigDecimal("2000");
        BigDecimal partialAmount = new BigDecimal("1000");
        setupBankTransferReservation(reservationId, expectedAmount, ReservationStatus.PENDING_PAYMENT);
        
        // Should not throw exception, just log warning and return
        assertDoesNotThrow(() -> reservationService.confirmBankTransferPayment(reservationId, partialAmount));
        
        // Verify that save was never called (partial payment should not confirm)
        verify(reservationRepository, never()).save(any(Reservation.class));
    }


    @Test
    void testGetReservationExistingReservationShouldReturnReservation() {
        Long reservationId = 1L;
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        Reservation result = reservationService.getReservation(reservationId);
        assertNotNull(result);
        assertEquals(reservationId, result.getReservationId());
    }

    @Test
    void testGetReservationNonExistingReservationShouldThrowException() {
        Long reservationId = 999L;
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());
        assertThrows(ReservationNotFoundException.class,
                () -> reservationService.getReservation(reservationId));
    }

    @Test
    void testCancelPendingBankTransferReservationsShouldReturnCount() {
        LocalDate cutoffDate = LocalDate.now().plusDays(2);
        List<Long> idsToCancel = List.of(1L, 2L, 3L);
        when(reservationRepository.findPendingBankTransferIds(cutoffDate)).thenReturn(idsToCancel);
        when(reservationRepository.cancelPendingBankTransfers(cutoffDate)).thenReturn(3);
        int result = reservationService.cancelPendingBankTransferReservations();
        assertEquals(3, result);
        verify(reservationRepository).cancelPendingBankTransfers(cutoffDate);
    }


    private FeignException.NotFound createFeignNotFoundException() {
        Request request = Request.create(Request.HttpMethod.POST, "/payment/status",
                Collections.emptyMap(), null, new RequestTemplate());
        return new FeignException.NotFound("Not Found", request, null, null);
    }

    private FeignException.BadRequest createFeignBadRequestException() {
        Request request = Request.create(Request.HttpMethod.POST, "/payment/status",
                Collections.emptyMap(), null, new RequestTemplate());
        return new FeignException.BadRequest("Bad Request", request, null, null);
    }

    private FeignException.InternalServerError createFeignInternalServerException() {
        Request request = Request.create(Request.HttpMethod.POST, "/payment/status",
                Collections.emptyMap(), null, new RequestTemplate());
        return new FeignException.InternalServerError("Internal Server Error", request, null, null);
    }

    private FeignException createFeignServiceUnavailableException() {
        Request request = Request.create(Request.HttpMethod.POST, "/payment/status",
                Collections.emptyMap(), null, new RequestTemplate());
        return new FeignException.FeignServerException(503, "Service Unavailable", request, null, null);
    }
}

