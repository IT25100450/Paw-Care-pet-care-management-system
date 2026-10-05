package com.example.pawcare.controller;

import com.example.pawcare.model.Product;
import com.example.pawcare.model.ProductCategory;
import com.example.pawcare.repository.ProductCategoryRepository;
import com.example.pawcare.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired private ProductRepository productRepository;
    @Autowired private ProductCategoryRepository productCategoryRepository;

    /* ── GET all products ────────────────────────────────────────── */
    @GetMapping
    public List<Map<String, Object>> getAll() {
        return productRepository.findAllWithCategory().stream()
                .map(this::toView).collect(Collectors.toList());
    }

    /* ── GET all categories ──────────────────────────────────────── */
    @GetMapping("/categories")
    public List<ProductCategory> getCategories() {
        return productCategoryRepository.findAll();
    }

    /* ── POST create product ─────────────────────────────────────── */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        try {
            String name = (String) body.get("productName");
            if (name == null || name.isBlank())
                return ResponseEntity.badRequest().body(Map.of("error", "Product name is required"));

            Integer categoryId = body.get("productCategoryId") instanceof Integer ?
                    (Integer) body.get("productCategoryId") : Integer.parseInt(body.get("productCategoryId").toString());

            ProductCategory category = productCategoryRepository.findById(categoryId).orElse(null);
            if (category == null)
                return ResponseEntity.badRequest().body(Map.of("error", "Category not found"));

            Product product = new Product();
            product.setProductName(name);
            product.setProductCategory(category);
            product.setPrice(BigDecimal.valueOf(Double.parseDouble(body.getOrDefault("price", "0").toString())));
            product.setQuantity(Integer.parseInt(body.getOrDefault("quantity", "0").toString()));
            product.setDescription((String) body.get("description"));
            product.setImageUrl((String) body.get("imageUrl"));

            Product saved = productRepository.save(product);
            return ResponseEntity.ok(toView(saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /* ── PUT update product ──────────────────────────────────────── */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) return ResponseEntity.notFound().build();
        try {
            if (body.get("productName") != null) {
                String n = body.get("productName").toString().trim();
                if (n.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "Product name is required"));
                product.setProductName(n);
            }
            if (body.get("productCategoryId") != null) {
                Integer catId = Integer.parseInt(body.get("productCategoryId").toString());
                ProductCategory cat = productCategoryRepository.findById(catId).orElse(null);
                if (cat == null) return ResponseEntity.badRequest().body(Map.of("error", "Category not found"));
                product.setProductCategory(cat);
            }
            if (body.get("price") != null) product.setPrice(BigDecimal.valueOf(Double.parseDouble(body.get("price").toString())));
            if (body.get("quantity") != null) product.setQuantity(Integer.parseInt(body.get("quantity").toString()));
            if (body.get("description") != null) product.setDescription(body.get("description").toString());
            if (body.get("imageUrl") != null) product.setImageUrl(body.get("imageUrl").toString());
            Product saved = productRepository.save(product);
            return ResponseEntity.ok(toView(saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Invalid data"));
        }
    }

    /* ── DELETE product ──────────────────────────────────────────── */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        if (!productRepository.existsById(id)) return ResponseEntity.notFound().build();
        productRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    private Map<String, Object> toView(Product p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("productId", p.getProductId());
        m.put("productName", p.getProductName());
        m.put("price", p.getPrice());
        m.put("quantity", p.getQuantity());
        m.put("description", p.getDescription());
        m.put("imageUrl", p.getImageUrl());
        m.put("categoryName", p.getProductCategory() != null ? p.getProductCategory().getProductCategoryName() : null);
        m.put("categoryId", p.getProductCategory() != null ? p.getProductCategory().getProductCategoryId() : null);
        return m;
    }
}
