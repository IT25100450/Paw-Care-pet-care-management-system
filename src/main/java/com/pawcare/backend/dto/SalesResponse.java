package com.pawcare.backend.dto;

import java.math.BigDecimal;

public class SalesResponse {

    private BigDecimal totalSales;
    private long totalOrders;

    public SalesResponse() {
    }

    public SalesResponse(BigDecimal totalSales, long totalOrders) {
        this.totalSales = totalSales;
        this.totalOrders = totalOrders;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }
}