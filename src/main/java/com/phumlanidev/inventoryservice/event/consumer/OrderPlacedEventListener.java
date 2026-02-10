package com.phumlanidev.inventoryservice.event.consumer;



import com.phumlanidev.commonevents.events.StockReservationFailedEvent;
import com.phumlanidev.commonevents.events.StockReservedEvent;
import com.phumlanidev.commonevents.events.order.OrderPlacedEvent;
import com.phumlanidev.inventoryservice.dto.StockRequestDto;
import com.phumlanidev.inventoryservice.service.impl.InventoryServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class OrderPlacedEventListener extends BaseEventListener<OrderPlacedEvent>{

  private final InventoryServiceImpl inventoryService;
  private final KafkaTemplate<String, StockReservedEvent> stockReservedEventKafkaTemplate;
  private final KafkaTemplate<String, StockReservationFailedEvent> stockReservationFailedEventKafkaTemplate;
//  private final StreamBridge streamBridge;

  public OrderPlacedEventListener(
          KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate,
          InventoryServiceImpl inventoryService,
          KafkaTemplate<String, StockReservedEvent> stockReservedEventKafkaTemplate,
          KafkaTemplate<String, StockReservationFailedEvent> stockReservationFailedEventKafkaTemplate) {
    super(kafkaTemplate, "order-placed-dlq");
    this.inventoryService = inventoryService;
    this.stockReservedEventKafkaTemplate = stockReservedEventKafkaTemplate;
    this.stockReservationFailedEventKafkaTemplate = stockReservationFailedEventKafkaTemplate;
  }

  @Retryable(
          backoff = @Backoff(delay = 1000, multiplier = 2),
          retryFor = {RecoverableDataAccessException.class},
          noRetryFor = {IllegalAccessException.class}
  )
  @KafkaListener(
          topics = "order.placed",
          groupId = "inventory-group",
          containerFactory = "orderKafkaListenerContainerFactory",
          errorHandler = "orderKafkaListenerErrorHandler"
  )
  public void reserveStock(ConsumerRecord<String, OrderPlacedEvent> record) {
    OrderPlacedEvent event = record.value();
    log.info("📦 Inventory reserving for order: {}", event.getOrderId());

    List<StockRequestDto> reserved = new ArrayList<>();
    try {
      for (OrderPlacedEvent.OrderItemDto item : event.getItems()) {
        StockRequestDto stockRequestDto = StockRequestDto.builder()
                .productId(item.getProductId())
                .quantity(item.getQuantity())
                .build();

        inventoryService.reserveStock(stockRequestDto);
        reserved.add(stockRequestDto);
        log.info("✅ Reserved {} units of product ID: {}", item.getQuantity(), item.getProductId());
      }

      StockReservedEvent reservedEvent = new StockReservedEvent(
              event.getOrderId(), event.getUserId(), event.getItems(), Instant.now()
      );
      stockReservedEventKafkaTemplate.send("stock.reserved", reservedEvent);
      log.info("✅ Stock reserved event sent for order: {}", event.getOrderId());

    } catch (Exception e) {
      log.error("❌ Stock reservation failed. Releasing previously reserved stock.", e);

      reserved.forEach(inventoryService::releaseStock);

      StockReservationFailedEvent failedEvent = new StockReservationFailedEvent(
              event.getOrderId(), event.getUserId(),
              "Stock reservation failed. " + e.getMessage(),
              Instant.now()
      );
      stockReservationFailedEventKafkaTemplate.send("stock.reservation.failed", failedEvent);
      throw e;
    }
  }
}
