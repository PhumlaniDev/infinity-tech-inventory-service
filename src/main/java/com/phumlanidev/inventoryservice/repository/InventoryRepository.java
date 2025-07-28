package com.phumlanidev.inventoryservice.repository;

import com.phumlanidev.inventoryservice.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {

  Optional<InventoryItem> findByProductId(Long productId);
}
