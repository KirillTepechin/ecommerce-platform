package com.example.inventory.service;

import com.example.inventory.model.ProductInventory;
import com.example.inventory.repository.ProductInventoryRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductInventoryService {

    private final ProductInventoryRepository inventoryRepository;

    @Retryable(
            retryFor = OptimisticLockException.class,
            backoff = @Backoff(delay = 100)
    )
    public boolean reserveProduct(String productId, Integer quantity, Long orderId) {
        final ProductInventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        if (inventory.canReserve(quantity)) {
            inventory.reserve(quantity);
            inventoryRepository.save(inventory);
            log.info("Reserved {} units of product {} for order {}", quantity, productId, orderId);
            return true;
        } else {
            log.warn("Insufficient inventory for product {}: requested {}, available {}",
                    productId, quantity, inventory.getAvailableQuantity());
            return false;
        }
    }

    public void releaseProduct(String productId, Integer quantity, Long orderId) {
        final ProductInventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        inventory.release(quantity);
        inventoryRepository.save(inventory);
        log.info("Released {} units of product {} for order {}", quantity, productId, orderId);
    }

    public void commitProductReservation(String productId, Integer quantity, Long orderId) {
        final ProductInventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        inventory.commitReservation(quantity);
        inventoryRepository.save(inventory);
        log.info("Committed reservation of {} units of product {} for order {}", quantity, productId, orderId);
    }
}
