package com.phumlanidev.inventoryservice.event.consumer;



import com.phumlanidev.commonevents.events.product.ProductCreatedEvent;
import com.phumlanidev.inventoryservice.event.dlq.ProductCreatedEventDlqPublisher;
import com.phumlanidev.inventoryservice.service.impl.InventoryServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProductCreatedEventListener extends BaseEventListener<ProductCreatedEvent>{

  private final InventoryServiceImpl inventoryService;
  private final ProductCreatedEventDlqPublisher eventDlqPublisher;

  public ProductCreatedEventListener(
          KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate,
          InventoryServiceImpl inventoryService,
          ProductCreatedEventDlqPublisher eventDlqPublisher) {
    super(kafkaTemplate, "product-created-dlq");
    this.inventoryService = inventoryService;
    this.eventDlqPublisher = eventDlqPublisher;
  }

  @Retryable(
          backoff = @Backoff(delay = 1000, multiplier = 2),
          retryFor = { RecoverableDataAccessException.class },
          noRetryFor = { IllegalAccessError.class }
  )
  @KafkaListener(
          topics = "product.created",
          groupId = "inventory-group",
          containerFactory = "productCreatedEventKafkaListenerContainerFactory",
          errorHandler = "productKafkaListenerErrorHandler"
  )
  public void consumeProductCreated(ConsumerRecord<String, ProductCreatedEvent> record) {
    ProductCreatedEvent event = record.value();
    log.info("Received ProductCreatedEvent: {}", event);
    try {
      if (event.getInitialQuantity() < 0) {
        throw new IllegalArgumentException("Initial quantity cannot be negative");
      }

      inventoryService.addStock(event.getProductId(), event.getInitialQuantity());
    } catch (Exception ex) {
      log.error("Failed to process event: {}", event, ex);
      throw ex;
    }
  }

  public void handleError(ConsumerRecord<String, ProductCreatedEvent> record, Exception ex) {
    eventDlqPublisher.publishToDlq(record.key(), record.value(), ex);
  }
}
