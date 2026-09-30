package com.pawcare.backend.repository;

import com.pawcare.backend.entity.CartItem;
import com.pawcare.backend.entity.User;
import com.pawcare.backend.entity.Product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository
        extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUser(User user);

    Optional<CartItem> findByUserAndProduct(
            User user,
            Product product
    );

    Optional<CartItem> findByIdAndUser(
            Long id,
            User user
    );
}