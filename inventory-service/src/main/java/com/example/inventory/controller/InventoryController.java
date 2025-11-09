package com.example.inventory.controller;

import com.example.inventory.model.ProductInventory;
import com.example.inventory.repository.ProductInventoryRepository;
import com.example.inventory.service.ProductInventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory API", description = "APIs for inventory management")
public class InventoryController {

    private final ProductInventoryRepository inventoryRepository;

    @Operation(summary = "Get all products inventory")
    @GetMapping
    public ResponseEntity<List<ProductInventory>> getAllInventory() {
        return ResponseEntity.ok(inventoryRepository.findAll());
    }

    @Operation(summary = "Get product inventory by ID")
    @GetMapping("/{productId}")
    public ResponseEntity<ProductInventory> getProductInventory(@PathVariable String productId) {
        ProductInventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        return ResponseEntity.ok(inventory);
    }

    @Operation(summary = "Get low stock products")
    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductInventory>> getLowStockProducts() {
        return ResponseEntity.ok(inventoryRepository.findLowStockProducts(10)); // threshold = 10
    }
}
