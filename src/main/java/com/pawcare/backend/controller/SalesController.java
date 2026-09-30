package com.pawcare.backend.controller;

import com.pawcare.backend.dto.SalesResponse;
import com.pawcare.backend.dto.SalesItemResponse;
import com.pawcare.backend.dto.SalesRecordResponse;
import com.pawcare.backend.entity.Checkout;
import com.pawcare.backend.entity.CheckoutItem;
import com.pawcare.backend.service.CheckoutService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sales")
@CrossOrigin(origins = "http://localhost:5173")
public class SalesController {

    private final CheckoutService checkoutService;

    public SalesController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    // GET ALL SALES
    @GetMapping
    public ResponseEntity<List<SalesRecordResponse>> getAllSales() {

        return ResponseEntity.ok(
                checkoutService.getAllSales()
                        .stream()
                        .map(this::toSalesRecord)
                        .toList()
        );
    }

    private SalesRecordResponse toSalesRecord(Checkout checkout) {

        List<SalesItemResponse> items = checkout.getItems()
                .stream()
                .map(this::toSalesItem)
                .toList();

        return new SalesRecordResponse(
                checkout.getId(),
                checkout.getTotalAmount(),
                checkout.getCheckoutDate(),
                items
        );
    }

    private SalesItemResponse toSalesItem(CheckoutItem item) {

        return new SalesItemResponse(
                item.getProduct().getId(),
                item.getProduct().getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()
        );
    }

    // GET SALES SUMMARY
    @GetMapping("/summary")
    public ResponseEntity<SalesResponse> getSalesSummary() {

        BigDecimal totalSales =
                checkoutService.getTotalSales();

        long totalOrders =
                checkoutService.getTotalOrders();

        SalesResponse response = new SalesResponse(
                totalSales,
                totalOrders
        );

        return ResponseEntity.ok(response);
    }

    // GET SALES BETWEEN DATES
    @GetMapping("/range")
    public ResponseEntity<List<Checkout>> getSalesBetween(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate
    ) {

        return ResponseEntity.ok(
                checkoutService.getSalesBetween(
                        startDate,
                        endDate
                )
        );
    }

    // GET SALES SUMMARY BETWEEN DATES
    @GetMapping("/range/summary")
    public ResponseEntity<SalesResponse> getSalesSummaryBetween(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate
    ) {

        BigDecimal totalSales =
                checkoutService.getSalesAmountBetween(
                        startDate,
                        endDate
                );

        long totalOrders =
                checkoutService.getOrdersBetween(
                        startDate,
                        endDate
                );

        SalesResponse response = new SalesResponse(
                totalSales,
                totalOrders
        );

        return ResponseEntity.ok(response);
    }
}
