package com.phumlanidev.inventoryservice.event.dlq;

import com.phumlanidev.commonevents.events.ProductCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductCreatedEventDlqPublisher {

  private final KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;
  private static final String DLQ_TOPIC = "product-created-dlq";

  public void publishToDlq(String key, ProductCreatedEvent event, Exception ex) {
    log.error("Publishing to DLQ: {}, error: {}", event, ex.getMessage());

    int attempts = 0;
    int maxAttempts = 5;
    long backOff = 1000;

    while (attempts < maxAttempts) {
      try {
        kafkaTemplate.send(DLQ_TOPIC, key, event);
        break;
      } catch (Exception e) {
        attempts++;
        log.warn("DLQ publish attempt {} failed: {}", attempts, e.getMessage());
        try {
          Thread.sleep(backOff);
          backOff *= 2;
        } catch (InterruptedException exc) {
          Thread.currentThread().interrupt();
        }
      }
    }

  }
}