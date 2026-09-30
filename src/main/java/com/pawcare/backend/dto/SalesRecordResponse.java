package com.pawcare.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SalesRecordResponse {

    private Long id;
    private BigDecimal totalAmount;
    private LocalDateTime checkoutDate;
    private List<SalesItemResponse> items;

    public SalesRecordResponse(
            Long id,
            BigDecimal totalAmount,
            LocalDateTime checkoutDate,
            List<SalesItemResponse> items
    ) {
        this.id = id;
        this.totalAmount = totalAmount;
        this.checkoutDate = checkoutDate;
        this.items = items;
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getCheckoutDate() {
        return checkoutDate;
    }

    public List<SalesItemResponse> getItems() {
        return items;
    }
}
