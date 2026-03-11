package com.example.inventory.service;

import event.OrderItemEvent;
import com.example.inventory.model.InventoryReservation;
import com.example.inventory.model.enums.ReservationStatus;
import com.example.inventory.repository.InventoryReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class InventoryReservationService {

    private final InventoryReservationRepository reservationRepository;
    private final ProductInventoryService inventoryService;

    public void createPendingReservation(Long orderId, List<OrderItemEvent> items) {
        log.info("Creating pending reservation for order: {}", orderId);

        for (OrderItemEvent item : items) {
            InventoryReservation reservation = InventoryReservation.builder()
                    .orderId(orderId)
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .status(ReservationStatus.PENDING)
                    .build();

            reservationRepository.save(reservation);
            log.debug("Created pending reservation for product {} in order {}",
                    item.getProductId(), orderId);
        }
    }

    public void updateReservationStatus(Long orderId, ReservationStatus status) {
        log.info("Updating reservation status to {} for order: {}", status, orderId);

        final List<InventoryReservation> reservations = reservationRepository.findByOrderId(orderId);

        for (InventoryReservation reservation : reservations) {
            reservation.setStatus(status);
            reservationRepository.save(reservation);
        }

        log.debug("Updated {} reservations for order {}", reservations.size(), orderId);
    }

    public void commitReservation(Long orderId) {
        log.info("Committing reservation for order: {}", orderId);

        final List<InventoryReservation> reservations = reservationRepository.findByOrderId(orderId);

        for (InventoryReservation reservation : reservations) {
            if (reservation.getStatus() == ReservationStatus.RESERVED) {
                // Подтверждаем резервирование в inventory
                inventoryService.commitProductReservation(
                        reservation.getProductId(),
                        reservation.getQuantity(),
                        orderId
                );

                // Меняем статус резервирования
                reservation.setStatus(ReservationStatus.COMMITTED);
                reservationRepository.save(reservation);

                log.debug("Committed reservation for product {} in order {}",
                        reservation.getProductId(), orderId);
            }
        }
    }

    public void cancelReservation(Long orderId) {
        log.info("Cancelling reservation for order: {}", orderId);

        final List<InventoryReservation> reservations = reservationRepository.findByOrderId(orderId);

        for (InventoryReservation reservation : reservations) {
            if (reservation.getStatus() == ReservationStatus.RESERVED ||
                    reservation.getStatus() == ReservationStatus.PENDING) {

                // Возвращаем товары на склад если они были зарезервированы
                if (reservation.getStatus() == ReservationStatus.RESERVED) {
                    inventoryService.releaseProduct(
                            reservation.getProductId(),
                            reservation.getQuantity(),
                            orderId
                    );
                }

                // Меняем статус на CANCELLED
                reservation.setStatus(ReservationStatus.CANCELLED);
                reservationRepository.save(reservation);

                log.debug("Cancelled reservation for product {} in order {}",
                        reservation.getProductId(), orderId);
            }
        }
    }

}
