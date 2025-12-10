package com.reservation.hotel.kafka;

import com.reservation.hotel.dto.BankTransferPaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankTransferPaymentUpdateProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "bank-transfer-payment-update";

    public void producePaymentUpdates(BankTransferPaymentEvent event, int ttlSeconds) {

        Message<BankTransferPaymentEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, TOPIC)
                .setHeader("message_ttl", ttlSeconds * 1000)
                .build();

        kafkaTemplate.send(message);
        log.info("Sending message with TTL {} seconds : {}", ttlSeconds, event);
    }


}
