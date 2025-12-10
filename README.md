# hotel-management-api

# Marvel Hospitality Hotel Reservation Service

## Overview
The **Hotel Reservation Service** is a backend microservice responsible for creation, validation, and confirmation of hotel reservations.  
It supports synchronous payment confirmation via credit-card integration and asynchronous bank-transfer confirmation using an event-driven architecture via Kafka.

This service is built following enterprise patterns, including:
- DTO-based request/response contracts
- Centralized validation and exception management
- Event-driven eventual consistency workflow

---

## Functional Capabilities

| Feature | Description |
|--------|-------------|
| Reservation Creation | Validates customer inputs, room type, dates, and pricing |
| Credit Card Payment | Calls external card service using OpenFeign |
| Bank Transfer Payment | Consumes Kafka topic `bank-transfer-payment-update` |
| Auto Cancellation | Scheduled job cancels unpaid reservations |
| Global Error Response | Consistent JSON error structure |
| Traceability | Request tracing via `X-Trace-Id` |

---

## Architecture

This solution follows a layered and modular approach:

Payment Processing Flow
Payment Mode	Workflow
Credit Card	Synchronous. External service responds with CONFIRMED/REJECTED/PENDING
Bank Transfer	Asynchronous. Kafka event updates reservation status
Cash	Auto-confirmed instantly

Bank Transfer Event Message Format
<E2E unique id (10 chars)> <reservationId (8 chars)>



## 🧾 REST Endpoints — Hotel Reservation Service
Method	Endpoint	Description
POST	/api/reservations/confirm	Create reservation & confirm payment based on payment mode

## Request:

{
  "paymentReference": "PR123456"
}


## Response:

{
  "lastUpdateDate": "2025-12-10T12:00:00Z",
  "status": "CONFIRMED"
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
  "timestamp": "2025-12-10T03:10:00.154Z",
  "status": 400,
  "error": "Validation Failed",
  "path": "/api/reservations/confirm",
  "traceId": "a781-223b-556",
  "errors": [
    {
      "field": "startDate",
      "code": "VAL_DATE_INVALID_FORMAT",
      "message": "Expected date yyyy-MM-dd"
    }
  ]
}

## Scheduler

Automatically cancels bank transfer reservations after the allowed time:
scheduler.reservation-cancellation.cron=0 0 1 * * ?

#Jacoco coverage enforced at:

80% instruction coverage
Excludes generated OpenAPI client module

## CI/CD & Deployment (Guidelines):

Stage	Action
Build	mvn clean package
Test	Run JUnit + Mockito

## Environment-based configuration is externalized via application-{profile}.yml.


## Author

Rohit Kurapatti
Java | Spring Boot | Microservices | Kafka

