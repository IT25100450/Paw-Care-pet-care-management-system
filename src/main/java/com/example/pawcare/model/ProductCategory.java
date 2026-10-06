package com.example.pawcare.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Product_Category")
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Product_Category_ID")
    private Integer productCategoryId;

    @Column(name = "Product_Category_Name", nullable = false, length = 25)
    private String productCategoryName;

    public ProductCategory() {}

    public Integer getProductCategoryId() { return productCategoryId; }
    public void setProductCategoryId(Integer productCategoryId) { this.productCategoryId = productCategoryId; }

    public String getProductCategoryName() { return productCategoryName; }
    public void setProductCategoryName(String productCategoryName) { this.productCategoryName = productCategoryName; }
}
