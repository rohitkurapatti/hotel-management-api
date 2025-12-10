package com.reservation.hotel.kafka;

import com.reservation.hotel.dto.BankTransferPaymentEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankTransferPaymentUpdateProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private BankTransferPaymentUpdateProducer producer;

    @Test
    void testProducePaymentUpdatesShouldSendMessageWithCorrectTopicAndTTL() {

        BankTransferPaymentEvent event = new BankTransferPaymentEvent();
        event.setPaymentId("PAY123456");
        event.setDebtorAccountNumber("1234567890");
        event.setAmountReceived(new BigDecimal("2000.00"));
        event.setTransactionDescription("1401541457 P0000001");

        int ttlSeconds = 300;

        ArgumentCaptor<Message<BankTransferPaymentEvent>> messageCaptor = ArgumentCaptor.forClass(Message.class);
        producer.producePaymentUpdates(event, ttlSeconds);
        verify(kafkaTemplate, times(1)).send(messageCaptor.capture());

        Message<BankTransferPaymentEvent> capturedMessage = messageCaptor.getValue();
        assertNotNull(capturedMessage);
        assertEquals(event, capturedMessage.getPayload());
        assertEquals("bank-transfer-payment-update", capturedMessage.getHeaders().get(KafkaHeaders.TOPIC));
        assertEquals(300000, capturedMessage.getHeaders().get("message_ttl")); // 300 seconds * 1000 = 300000 ms
    }
}

