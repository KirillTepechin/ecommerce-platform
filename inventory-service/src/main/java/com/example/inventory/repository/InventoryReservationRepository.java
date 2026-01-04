package com.example.inventory.repository;

import com.example.inventory.model.InventoryReservation;
import com.example.inventory.model.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {
    List<InventoryReservation> findByOrderId(Long orderId);

    List<InventoryReservation> findByStatus(ReservationStatus status);

    @Query("SELECT r FROM InventoryReservation r WHERE r.orderId = :orderId AND r.productId = :productId")
    List<InventoryReservation> findByOrderIdAndProductId(Long orderId, String productId);

    boolean existsByOrderIdAndStatus(Long orderId, ReservationStatus status);
}
