package com.pawcare.backend.service;

import com.pawcare.backend.dto.AddToCartRequest;
import com.pawcare.backend.dto.UpdateCartRequest;
import com.pawcare.backend.entity.CartItem;
import com.pawcare.backend.entity.Product;
import com.pawcare.backend.entity.User;
import com.pawcare.backend.repository.CartItemRepository;
import com.pawcare.backend.repository.ProductRepository;
import com.pawcare.backend.repository.UserRepository;

import org.springframework.stereotype.Service;

import com.pawcare.backend.dto.CartItemResponse;
import com.pawcare.backend.dto.CartResponse;

import java.math.BigDecimal;
import java.util.stream.Collectors;

import java.util.List;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository
    ) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public CartItem addToCart(
            String email,
            AddToCartRequest request
    ) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        Product product = productRepository.findById(
                request.getProductId()
        ).orElseThrow(() ->
                new RuntimeException("Product not found")
        );

        if (request.getQuantity() >
                product.getStockQuantity()) {

            throw new RuntimeException(
                    "Requested quantity exceeds available stock"
            );
        }

        var existingCartItem =
                cartItemRepository.findByUserAndProduct(
                        user,
                        product
                );

        if (existingCartItem.isPresent()) {

            CartItem cartItem =
                    existingCartItem.get();

            int newQuantity =
                    cartItem.getQuantity()
                            + request.getQuantity();

            if (newQuantity >
                    product.getStockQuantity()) {

                throw new RuntimeException(
                        "Requested quantity exceeds available stock"
                );
            }

            cartItem.setQuantity(newQuantity);

            return cartItemRepository.save(cartItem);
        }

        CartItem cartItem = new CartItem();

        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setQuantity(request.getQuantity());

        return cartItemRepository.save(cartItem);
    }

    public CartResponse getCart(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        List<CartItem> cartItems =
                cartItemRepository.findByUser(user);

        List<CartItemResponse> items =
                cartItems.stream()
                        .map(cartItem -> {

                            Product product =
                                    cartItem.getProduct();

                            BigDecimal subtotal =
                                    product.getPrice()
                                            .multiply(
                                                    BigDecimal.valueOf(
                                                            cartItem.getQuantity()
                                                    )
                                            );

                            return new CartItemResponse(
                                    cartItem.getId(),
                                    product.getId(),
                                    product.getProductName(),
                                    product.getPrice(),
                                    cartItem.getQuantity(),
                                    subtotal,
                                    product.getImage()
                            );
                        })
                        .collect(Collectors.toList());

        Integer totalItems =
                cartItems.stream()
                        .mapToInt(CartItem::getQuantity)
                        .sum();

        BigDecimal totalAmount =
                items.stream()
                        .map(CartItemResponse::getSubtotal)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        return new CartResponse(
                items,
                totalItems,
                totalAmount
        );
    }

    public CartItem updateCartItem(
            String email,
            Long cartItemId,
            UpdateCartRequest request
    ) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        CartItem cartItem =
                cartItemRepository
                        .findByIdAndUser(cartItemId, user)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Cart item not found"
                                )
                        );

        Product product = cartItem.getProduct();

        if (request.getQuantity() >
                product.getStockQuantity()) {

            throw new RuntimeException(
                    "Requested quantity exceeds available stock"
            );
        }

        cartItem.setQuantity(
                request.getQuantity()
        );

        return cartItemRepository.save(cartItem);
    }

    public void removeFromCart(
            String email,
            Long cartItemId
    ) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        CartItem cartItem =
                cartItemRepository
                        .findByIdAndUser(cartItemId, user)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Cart item not found"
                                )
                        );

        cartItemRepository.delete(cartItem);
    }
}