package com.example.inventory.repository;

import com.example.inventory.model.ProductInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductInventoryRepository extends JpaRepository<ProductInventory, String> {

    @Query("SELECT p FROM ProductInventory p WHERE p.availableQuantity < :threshold")
    List<ProductInventory> findLowStockProducts(Integer threshold);

}
