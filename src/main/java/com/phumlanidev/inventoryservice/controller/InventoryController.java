package com.phumlanidev.inventoryservice.controller;

import com.phumlanidev.inventoryservice.dto.StockRequestDto;
import com.phumlanidev.inventoryservice.dto.StockResponseDto;
import com.phumlanidev.inventoryservice.service.impl.InventoryServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

  private final InventoryServiceImpl inventoryService;

  @PostMapping("/reserve")
  public ResponseEntity<Void> reserveStock(@RequestBody StockRequestDto request) {
    inventoryService.reserveStock(request);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/release")
  public ResponseEntity<Void> releaseStock(@RequestBody StockRequestDto request) {
    inventoryService.releaseStock(request);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/deduct")
  public ResponseEntity<Void> deductStock(@RequestBody StockRequestDto request) {
    inventoryService.deductStock(request);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/{productId}")
  public ResponseEntity<StockResponseDto> getStock(@PathVariable Long productId) {
    return ResponseEntity.ok(inventoryService.getStock(productId));
  }
}
