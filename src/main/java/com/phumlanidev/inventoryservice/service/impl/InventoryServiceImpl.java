package com.phumlanidev.inventoryservice.service.impl;

import com.phumlanidev.inventoryservice.constant.Constant;
import com.phumlanidev.inventoryservice.dto.StockRequestDto;
import com.phumlanidev.inventoryservice.dto.StockResponseDto;
import com.phumlanidev.inventoryservice.exception.inventory.InsufficientStockException;
import com.phumlanidev.inventoryservice.exception.inventory.ProductNotFoundException;
import com.phumlanidev.inventoryservice.model.InventoryItem;
import com.phumlanidev.inventoryservice.repository.InventoryRepository;
import com.phumlanidev.inventoryservice.service.InventoryService;
import com.phumlanidev.inventoryservice.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

  private final InventoryRepository inventoryRepository;
  private final AuditLogService auditLogService;
  private final SecurityUtils securityUtils;

  @Override
  public void reserveStock(StockRequestDto request) {
    log.debug("🔍 Reserving stock for productId: {}", request.getProductId());
    InventoryItem item = inventoryRepository.findByProductId(request.getProductId())
            .orElseThrow(() -> new ProductNotFoundException(Constant.PRODUCT_NOT_FOUND));
    log.debug("✅ Found stock: {}", item);

    if (item.getAvailableQuantity() < request.getQuantity()) {
      throw new InsufficientStockException(Constant.INSUFFICIENT_STOCK);
    }

    item.setAvailableQuantity(item.getAvailableQuantity() - request.getQuantity());
    item.setReservedQuantity(item.getReservedQuantity() + request.getQuantity());
    item.setUpdatedAt(Instant.now());

    inventoryRepository.save(item);
    logAudit("STOCK_RESERVED", "Reserved " + request.getQuantity() + " units for product ID: " + request.getProductId());
  }

  @Override
  public void releaseStock(StockRequestDto request) {
    InventoryItem item = inventoryRepository.findByProductId(request.getProductId())
            .orElseThrow(() -> new ProductNotFoundException(Constant.PRODUCT_NOT_FOUND));

    if (item.getReservedQuantity() < request.getQuantity()) {
      throw new InsufficientStockException(Constant.INSUFFICIENT_STOCK);
    }

    item.setAvailableQuantity(item.getAvailableQuantity() + request.getQuantity());
    item.setReservedQuantity(item.getReservedQuantity() - request.getQuantity());
    item.setUpdatedAt(Instant.now());

    inventoryRepository.save(item);
    logAudit("STOCK_RELEASED", "Released " + request.getQuantity() + " units for product ID: " + request.getProductId());
  }

  @Override
  public void deductStock(StockRequestDto request) {
    InventoryItem item = inventoryRepository.findByProductId(request.getProductId())
            .orElseThrow(() -> new ProductNotFoundException(Constant.PRODUCT_NOT_FOUND));

    if (item.getReservedQuantity() < request.getQuantity()) {
      throw new InsufficientStockException(Constant.INSUFFICIENT_STOCK);
    }

    item.setReservedQuantity(item.getReservedQuantity() - request.getQuantity());
    item.setUpdatedAt(Instant.now());

    inventoryRepository.save(item);
    logAudit("STOCK_DEDUCTED", "Deducted " + request.getQuantity() + " units for product ID: " + request.getProductId());
  }

  @Override
  public StockResponseDto getStock(Long productId) {
    InventoryItem item = inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new ProductNotFoundException(Constant.PRODUCT_NOT_FOUND));
    logAudit("STOCK_RETRIEVED", "Stock retrieved for product ID: " + productId);

    return StockResponseDto.builder()
            .productId(item.getProductId())
            .available(item.getAvailableQuantity())
            .reserved(item.getReservedQuantity())
            .success(true)
            .message("Stock retrieved successfully")
            .build();
  }

  @Override
  public void addStock(Long productId, int quantity) {
    InventoryItem item = inventoryRepository.findByProductId(productId)
            .orElse(InventoryItem.builder()
                    .productId(productId)
            .availableQuantity(0)
                    .reservedQuantity(0)
                    .updatedAt(Instant.now())
                    .build());
    item.setAvailableQuantity(item.getAvailableQuantity() + quantity);
    item.setUpdatedAt(Instant.now());
    inventoryRepository.save(item);
    logAudit("STOCK_ADDED", "Added " + quantity + " units for product ID: " + productId);
  }

  private void logAudit(String action, String details) {
    String clientIp = "SYSTEM";  // Since this is a system event, not HTTP
    String username = "SYSTEM";
    String userId = "SYSTEM";

    auditLogService.log(
            action,
            userId,
            username,
            clientIp,
            details
    );
  }
}
