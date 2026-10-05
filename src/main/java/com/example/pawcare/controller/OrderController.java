package com.example.pawcare.controller;

import com.example.pawcare.model.*;
import com.example.pawcare.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired private CustomerOrderRepository orderRepository;
    @Autowired private PetOwnerRepository petOwnerRepository;
    @Autowired private ProductRepository productRepository;

    /* ── GET orders for an owner ─────────────────────────────────── */
    @GetMapping("/owner/{ownerId}")
    public List<Map<String, Object>> getByOwner(@PathVariable Integer ownerId) {
        return orderRepository.findByOwnerOrderByDateDesc(ownerId)
                .stream().map(this::toView).collect(Collectors.toList());
    }

    /* ── POST place an order (confirmed cart) ────────────────────── */
    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody Map<String, Object> body) {
        try {
            Integer ownerId = ((Number) body.get("ownerId")).intValue();
            PetOwner owner = petOwnerRepository.findById(ownerId).orElse(null);
            if (owner == null) return ResponseEntity.badRequest().body(Map.of("error", "Owner not found"));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> cartItems = (List<Map<String, Object>>) body.get("items");
            if (cartItems == null || cartItems.isEmpty())
                return ResponseEntity.badRequest().body(Map.of("error", "Cart is empty"));

            CustomerOrder order = new CustomerOrder();
            order.setOwner(owner);
            order.setStatus("confirmed");
            CustomerOrder savedOrder = orderRepository.save(order);

            for (Map<String, Object> item : cartItems) {
                Integer productId = ((Number) item.get("productId")).intValue();
                Integer qty       = ((Number) item.get("quantity")).intValue();

                Product product = productRepository.findById(productId).orElse(null);
                if (product == null) continue;

                OrderItem oi = new OrderItem();
                oi.setOrder(savedOrder);
                oi.setProduct(product);
                oi.setQuantity(qty);
                oi.setUnitPrice(product.getPrice());
                savedOrder.getItems().add(oi);

                // Reduce stock
                product.setQuantity(Math.max(0, product.getQuantity() - qty));
                productRepository.save(product);
            }
            orderRepository.save(savedOrder);
            return ResponseEntity.ok(toView(savedOrder));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    /* ── PATCH cancel order and restore stock ─────────────────── */
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Integer orderId) {
        CustomerOrder order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return ResponseEntity.notFound().build();
        if ("cancelled".equals(order.getStatus()))
            return ResponseEntity.badRequest().body(Map.of("error", "Order already cancelled"));

        // Restore stock for each item
        for (OrderItem oi : order.getItems()) {
            Product product = oi.getProduct();
            product.setQuantity(product.getQuantity() + oi.getQuantity());
            productRepository.save(product);
        }

        order.setStatus("cancelled");
        orderRepository.save(order);
        return ResponseEntity.ok(toView(order));
    }

    private Map<String, Object> toView(CustomerOrder o) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("orderId",    o.getOrderId());
        m.put("orderDate",  o.getOrderDate() != null ? o.getOrderDate().toString() : null);
        m.put("status",     o.getStatus());
        m.put("ownerId",    o.getOwner().getOwnerId());
        m.put("ownerName",  o.getOwner().getFirstName() + " " + o.getOwner().getLastName());

        BigDecimal total = BigDecimal.ZERO;
        List<Map<String, Object>> items = new ArrayList<>();
        for (OrderItem oi : o.getItems()) {
            Map<String, Object> i = new LinkedHashMap<>();
            i.put("productId",   oi.getProduct().getProductId());
            i.put("productName", oi.getProduct().getProductName());
            i.put("quantity",    oi.getQuantity());
            i.put("unitPrice",   oi.getUnitPrice());
            BigDecimal lineTotal = oi.getUnitPrice().multiply(BigDecimal.valueOf(oi.getQuantity()));
            i.put("lineTotal",   lineTotal);
            total = total.add(lineTotal);
            items.add(i);
        }
        m.put("items",      items);
        m.put("totalPrice", total);
        return m;
    }
}
