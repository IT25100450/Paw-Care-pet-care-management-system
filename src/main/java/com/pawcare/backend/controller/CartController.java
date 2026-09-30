package com.pawcare.backend.controller;

import com.pawcare.backend.dto.AddToCartRequest;
import com.pawcare.backend.dto.UpdateCartRequest;
import com.pawcare.backend.entity.CartItem;
import com.pawcare.backend.service.CartService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.pawcare.backend.dto.CartResponse;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:5173")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    public ResponseEntity<CartItem> addToCart(
            Authentication authentication,
            @Valid @RequestBody AddToCartRequest request
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                cartService.addToCart(
                        email,
                        request
                )
        );
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                cartService.getCart(email)
        );
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<CartItem> updateCartItem(
            Authentication authentication,
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartRequest request
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                cartService.updateCartItem(
                        email,
                        cartItemId,
                        request
                )
        );
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> removeFromCart(
            Authentication authentication,
            @PathVariable Long cartItemId
    ) {

        String email = authentication.getName();

        cartService.removeFromCart(
                email,
                cartItemId
        );

        return ResponseEntity.noContent().build();
    }
}