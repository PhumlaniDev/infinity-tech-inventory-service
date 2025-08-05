package com.phumlanidev.inventoryservice.event.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
public abstract class BaseEventListener<T> {

  private final KafkaTemplate<String, T> kafkaTemplate;
  private final String dlqTopic;

  protected BaseEventListener(KafkaTemplate<String, T> kafkaTemplate, String dlqTopic) {
    this.kafkaTemplate = kafkaTemplate;
    this.dlqTopic = dlqTopic;
  }

  protected void processWithDlq(String key, T event, Runnable action) {
    try {
      action.run();
    } catch (Exception ex) {
      log.error("❌ Error processing event: {} - {}", event, ex.getMessage());
      publishToDlq(key, event, ex);
    }
  }

  private void publishToDlq(String key, T event, Exception ex) {
    log.error("📥 Sending to DLQ: {}, due to error: {}", event, ex.getMessage());
    kafkaTemplate.send(dlqTopic, key, event);
  }
}
