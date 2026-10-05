package com.example.pawcare.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "Order_Item")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Order_Item_ID")
    private Integer orderItemId;

    @ManyToOne
    @JoinColumn(name = "Order_ID", nullable = false)
    private CustomerOrder order;

    @ManyToOne
    @JoinColumn(name = "Product_ID", nullable = false)
    private Product product;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "Unit_Price", nullable = false)
    private BigDecimal unitPrice;

    public OrderItem() {}

    public Integer getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Integer orderItemId) { this.orderItemId = orderItemId; }

    public CustomerOrder getOrder() { return order; }
    public void setOrder(CustomerOrder order) { this.order = order; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}
