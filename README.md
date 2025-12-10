# Hotel Management API

A simple hotel reservation system built with Spring Boot. This project handles status of room booking, payments, and user authentication.

## What's Inside

- **Reservation Service** - Main service to confirm a room reservation
- **Credit Card Client** - Handles payment verification for credit card via feign client
- User authentication with JWT
- Kafka integration for bank transfers
- H2 database for dev/testing

## Getting Started

### Prerequisites

- Java 21
- Maven 3.x

The API will be available at `http://localhost:8080`

### Configuration

Before running, set your JWT secret key in the yaml:

```yaml
jwt:
  secret-key: ${JWT_SECRET_KEY}
```

## API Endpoints

### Authentication
- `POST /api/auth/register` - Sign up
- `POST /api/auth/login` - Login

Pass the Bearer Token for each request in the headers in form of 
Authorization: Bearer {{YOUR_BEARER_TOKEN}}

### Reservations
- `POST /api/reservations/confirm` - Create and confirm reservation based on payment mode, also get the status based on the payment

### Swagger UI
Open `http://localhost:8080/swagger-ui.html` to see all endpoints

## Payment Options

The system supports three payment methods:
1. **Cash** - Instant confirmation
2. **Credit Card** - Verified via external API. Provided a mock implementation for the external API under mocks folder, it runs at `http://localhost:8080/mock-credit-card`
3. **Bank Transfer** - Confirmed via Kafka events

## Testing

mvn test

## Notes

- Default port is 8080
- H2 console available at `/h2-console` (dev only)
    It contains default user and admin for login purpose
    It contains default status and payment reference to simulate the credit card payment
    It contains default data which can be used by Scheduler to cancel the pending payments based on the given criteria 
- All endpoints except `/api/auth/**` require authentication
- Kafka is optional (controlled by `spring.kafka.enabled`)

---

# Marvel Hospitality Hotel Reservation Service

## Overview
The **Hotel Reservation Service** is a backend microservice responsible for creation, validation, and confirmation of hotel reservations.  

This service is built following enterprise patterns, including:
- DTO-based request/response contracts
- Centralized validation and exception management
- Event-driven eventual consistency workflow

---


## Architecture

This solution follows a layered and modular approach:

Credit Card	Synchronous. External service responds with CONFIRMED/REJECTED/PENDING
Bank Transfer	Asynchronous. Kafka event updates reservation status : need kafka server up and running
Cash	Auto-confirmed instantly
Scheduler will automatically Cancel the reservation if total amount is not received 2 day before the reservation start date and it runs every midnight

Bank Transfer Event Message Format
<E2E unique id (10 chars)> <reservationId (8 chars)>



## 🧾 REST Endpoints — Hotel Reservation Service
Method	Endpoint	Description
POST	/api/reservations/confirm	Create reservation & confirm payment based on payment mode

## Request:

{
  {
  "customerName": "Allan",
  "roomNumber": "102",
  "startDate": "2025-12-13",
  "endDate": "2026-01-09",
  "roomSegment": "SMALL",
  "paymentMode": "CASH",
  "paymentReference": "PN333333"
}
}


## Response:

{
    "reservationId": 7,
    "reservationStatus": "CONFIRMED"
}


## 🧾 POST /mock-credit-card/payment-status`

reservtion service calls downstream credit card api
(Mock DB behind)


## 🔁 Event Driven — Bank Transfer
Kafka Topic — bank-transfer-payment-update


## Message Structure:

Attribute	Source
paymentId	Unique event ID
amountReceived	Payment amount
transactionDescription	Format <E2E_10_char> <reservationId_8_char>


This is consumed automatically:

@KafkaListener(topics = "bank-transfer-payment-update")
public void onBankTransferPayment(BankTransferPaymentEvent event);
Cancels any unpaid pending transfer past threshold.


## Validation & Error Management

The service applies multi-layer validation:
Bean validation (Null, Pattern, Custom Annotation)
JSON parsing validation (format issues)
Business rule enforcement
Unified structured error response:

{
    "timestamp": "2025-12-10T16:33:09.2557096",
    "status": 400,
    "error": "Validation Failed",
    "path": "/api/reservations/confirm",
    "traceId": "08335451-42c4-481f-b955-973d34d5f3e2",
    "errors": {
        "startDate": "Start date cannot be in the past"
    }
}

## Scheduler

Automatically cancels bank transfer reservations after the allowed time:
scheduler.reservation-cancellation.cron=0 0 0 * * *
You can modify the cron expression as per your needs in the application-{profile}.yml file
To simulate the scheduer you can use 3 minutes cron expression : */3 * * * * *

## Environment-based configuration is externalized via application-{profile}.yml.


## Author

Rohit Kurapatti
Java | Spring Boot | Microservices | Kafka

