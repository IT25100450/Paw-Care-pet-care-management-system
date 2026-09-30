package com.pawcare.backend.repository;

import com.pawcare.backend.entity.CheckoutItem;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckoutItemRepository extends JpaRepository<CheckoutItem, Long> {
}