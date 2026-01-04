package com.example.inventory.repository;

import com.example.inventory.model.ProductInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductInventoryRepository extends JpaRepository<ProductInventory, String> {

    Optional<ProductInventory> findByProductId(String productId);

    @Query("SELECT p FROM ProductInventory p WHERE p.availableQuantity > 0")
    List<ProductInventory> findAvailableProducts();

    @Query("SELECT p FROM ProductInventory p WHERE p.availableQuantity < :threshold")
    List<ProductInventory> findLowStockProducts(Integer threshold);

    boolean existsByProductId(String productId);
}
