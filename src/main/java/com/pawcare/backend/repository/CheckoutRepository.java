package com.pawcare.backend.repository;

import com.pawcare.backend.entity.Checkout;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface CheckoutRepository extends JpaRepository<Checkout, Long> {

    /*
     * Get all checkouts ordered from newest to oldest.
     * Useful for the frontend's Recent Sales section.
     */
    @EntityGraph(attributePaths = {"items", "items.product"})
    List<Checkout> findAllByOrderByCheckoutDateDesc();

    /*
     * Calculate total revenue from all completed checkouts.
     */
    @Query("""
           SELECT COALESCE(SUM(c.totalAmount), 0)
           FROM Checkout c
           """)
    BigDecimal getTotalSales();

    /*
     * Count the total number of completed checkouts/orders.
     */
    @Query("""
           SELECT COUNT(c)
           FROM Checkout c
           """)
    long getTotalOrders();

    /*
     * Get sales between two dates.
     * Useful for daily, weekly, monthly or custom sales reports.
     */
    List<Checkout> findByCheckoutDateBetweenOrderByCheckoutDateDesc(
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    /*
     * Calculate revenue between two dates.
     */
    @Query("""
           SELECT COALESCE(SUM(c.totalAmount), 0)
           FROM Checkout c
           WHERE c.checkoutDate BETWEEN :startDate AND :endDate
           """)
    BigDecimal getSalesBetween(
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    /*
     * Count orders between two dates.
     */
    @Query("""
           SELECT COUNT(c)
           FROM Checkout c
           WHERE c.checkoutDate BETWEEN :startDate AND :endDate
           """)
    long getOrdersBetween(
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}
