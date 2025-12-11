package com.reservation.hotel.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.creditcard.api.PaymentStatusApiClient;
import com.payment.creditcard.model.PaymentStatusResponse;
import com.payment.creditcard.model.PaymentStatusRetrievalRequest;
import com.reservation.hotel.commons.exception.PaymentNotConfirmedException;
import com.reservation.hotel.commons.exception.PaymentReferenceNotFoundException;
import com.reservation.hotel.commons.exception.ReservationNotFoundException;
import com.reservation.hotel.commons.exception.ServiceUnavailableException;
import com.reservation.hotel.model.ReservationRequest;
import com.reservation.hotel.model.ReservationResponse;
import com.reservation.hotel.model.PaymentMode;
import com.reservation.hotel.model.PaymentVerificationStatus;
import com.reservation.hotel.entities.Reservation;
import com.reservation.hotel.model.ReservationStatus;
import com.reservation.hotel.repository.ReservationRepository;
import com.reservation.hotel.services.ReservationService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;

    private final PaymentStatusApiClient paymentStatusApiClient;

    private final ObjectMapper objectMapper;
    @Override
    @Transactional
    public ReservationResponse confirmReservation(ReservationRequest reservationRequest) {

        log.debug("Inside method confirmReservation: ReservationRequest and setting the reservation values");

        // Check for existing reservation
        Optional<Reservation> existingReservation = reservationRepository
                .findFirstByCustomerNameAndRoomNumberAndStartDateAndEndDateAndRoomSegmentAndPaymentMode(
                        reservationRequest.getCustomerName(),
                        reservationRequest.getRoomNumber(),
                        reservationRequest.getStartDate(),
                        reservationRequest.getEndDate(),
                        reservationRequest.getRoomSegment(),
                        reservationRequest.getPaymentMode()
                );

        if (existingReservation.isPresent()) {
            log.info("Found existing reservation with ID: {}. Returning existing reservation (idempotent behavior)",
                    existingReservation.get().getReservationId());
            Reservation existing = existingReservation.get();
            return new ReservationResponse(existing.getReservationId(), existing.getReservationStatus());
        }

        // Used ObjectMapper to map ReservationRequest to Reservation
        Reservation reservation = objectMapper.convertValue(reservationRequest, Reservation.class);
        BigDecimal totalAmount = reservationRequest.getRoomSegment().getPricePerDay();
        reservation.setTotalAmount(totalAmount);

        //Setting initial payment status as PENDING
        reservation.setReservationStatus(ReservationStatus.PENDING_PAYMENT);

        switch (reservationRequest.getPaymentMode()) {

            case CASH -> {
                log.debug("Setting reservation status as CONFIRMED, mode of payment is CASH");
                reservation.setReservationStatus(ReservationStatus.CONFIRMED);

            }

            case CREDIT_CARD -> {
                PaymentVerificationStatus status =
                        verifyCreditCardPayment(reservationRequest.getPaymentReference());

                switch (status) {
                    case CONFIRMED -> {
                        log.debug("Full Payment done and confirmed by credit card");
                        reservation.setReservationStatus(ReservationStatus.CONFIRMED);
                    }
                    case CANCELLED, PENDING, REJECTED -> {
                        log.error("Credit Card Payment REJECTED");
                        throw new PaymentNotConfirmedException("Payment is in pending state or cancelled or rejected.");
                    }
                    case NOT_FOUND -> {
                        log.error("Credit Card Payment reference not found");
                        throw new PaymentReferenceNotFoundException("Payment reference not found");
                    }
                }
            }

            case BANK_TRANSFER -> {
                // keep PENDING_PAYMENT; will be confirmed via Kafka event
                log.debug("Keeping status as PENDING, it will be updated via Kafka");
                reservation.setReservationStatus(ReservationStatus.PENDING_PAYMENT);
            }
        }

        Reservation savedReservation = reservationRepository.save(reservation);
        return new ReservationResponse(savedReservation.getReservationId(), savedReservation.getReservationStatus());
    }

    @Transactional
    public void confirmBankTransferPayment(Long reservationId, BigDecimal amountReceived) {
        // log error if not found instead of throwing exception
        Optional<Reservation> reservation = reservationRepository.findById(reservationId);
        if (reservation.isEmpty()) {
            log.error("Cannot process bank transfer payment: Reservation not found for id {}", reservationId);
            return;
        }

        Reservation reservationEvent = reservation.get();

        if (reservationEvent.getPaymentMode() != PaymentMode.BANK_TRANSFER) {
            log.error("Cannot process bank transfer payment: Reservation {} has payment mode {} instead of BANK_TRANSFER",
                    reservationId, reservationEvent.getPaymentMode());
            return;
        }

        // Check if already cancelled - log warning
        if (reservationEvent.getReservationStatus() == ReservationStatus.CANCELLED) {
            log.warn("Cannot process bank transfer payment: Reservation {} is already cancelled", reservationId);
            return;
        }

        // Check if already confirmed
        if (reservationEvent.getReservationStatus() == ReservationStatus.CONFIRMED) {
            log.info("Bank transfer payment received for reservation {} but it's already confirmed. Skipping update.", reservationId);
            return;
        }

        BigDecimal expectedAmount = reservationEvent.getTotalAmount();
        //For partial payments keep the status pending
        if (amountReceived.compareTo(expectedAmount) < 0) {
            log.warn("Partial or insufficient payment received for reservation {}. Expected {}, Received {}. Keeping status as PENDING_PAYMENT",
                    reservationId, expectedAmount, amountReceived);
            return;
        }

        // FULL payment received
        log.info("Full bank payment received for reservation {}. Confirming booking.", reservationId);
        reservationEvent.setReservationStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(reservationEvent);
        log.info("Successfully confirmed reservation {} via bank transfer payment", reservationId);
    }

    private PaymentVerificationStatus verifyCreditCardPayment(String ref) {
        try {
            PaymentStatusResponse response = paymentStatusApiClient.paymentStatusPost(
                    new PaymentStatusRetrievalRequest(ref)).getBody();

            if (response == null || response.getStatus() == null) {
                log.error("Null response or status received from credit card payment service for reference: {}", ref);
                throw new PaymentNotConfirmedException("Unable to verify payment status");
            }
            String status = response.getStatus();

            return switch (status.toUpperCase()) {
                case "CONFIRMED" -> PaymentVerificationStatus.CONFIRMED;
                case "CANCELLED" -> PaymentVerificationStatus.CANCELLED;
                case "PENDING" -> PaymentVerificationStatus.PENDING;
                case "REJECTED" -> PaymentVerificationStatus.REJECTED;
                default -> {
                    log.warn("Unknown payment status '{}' received for reference: {}", status, ref);
                    yield PaymentVerificationStatus.PENDING;
                }
            };

        } catch (FeignException.NotFound ex) {
            // 404 - Payment reference not found
            log.error("Payment reference not found: {}", ref);
            return PaymentVerificationStatus.NOT_FOUND;

        } catch (FeignException.BadRequest ex) {
            // 400 - Invalid payment reference format
            log.error("Invalid payment reference format: {}. Error: {}", ref, ex.getMessage());
            throw new PaymentReferenceNotFoundException("Invalid payment reference format: " + ref);

        } catch (FeignException.InternalServerError ex) {
            // 500 - Credit card service internal error - return 503
            log.error("Credit card payment service internal error for reference: {}. Error: {}", ref, ex.getMessage());
            throw new ServiceUnavailableException("Credit card payment service is currently unavailable. Please try again later.");

        } catch (FeignException ex) {
            // Handle connection refused (status -1) and other network errors - return 503
            if (ex.status() == -1) {
                log.error("Credit card payment service is down or unreachable for reference: {}. Error: {}", ref, ex.getMessage());
                throw new ServiceUnavailableException("Credit card payment service is currently unavailable. Please try again later.");
            }

            // Handle 503 Service Unavailable from the payment service
            if (ex.status() == 503) {
                log.error("Credit card payment service unavailable for reference: {}. Error: {}", ref, ex.getMessage());
                throw new ServiceUnavailableException("Credit card payment service is currently unavailable. Please try again later.");
            }

            // Generic Feign exception (any other HTTP status) - return 500
            log.error("Unexpected error from credit card payment service for reference: {}. Status: {}, Error: {}",
                     ref, ex.status(), ex.getMessage());
            throw new PaymentNotConfirmedException("Unable to verify payment status. Please contact support.");
        }
    }

    public Reservation getReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found for id " + id));
    }

    @Override
    @Transactional
    public int cancelPendingBankTransferReservations() {
        LocalDate cutoffDate = LocalDate.now().plusDays(2);

        log.info("Cancelling pending bank transfer reservations with start date <= {}", cutoffDate);
        List<Long> idsToCancel = reservationRepository.findPendingBankTransferIds(cutoffDate);

        if (idsToCancel.isEmpty()) {
            log.info("No pending bank transfer reservations found to cancel");
            return 0;
        }
        int cancelledCount = reservationRepository.cancelPendingBankTransfers(cutoffDate);
        log.info("Successfully cancelled {} reservation(s) with IDs: {}", cancelledCount, idsToCancel);
        return cancelledCount;
    }


}
