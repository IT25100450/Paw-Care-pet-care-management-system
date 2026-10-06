package com.example.pawcare.repository;

import com.example.pawcare.model.Vet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VetRepository extends JpaRepository<Vet, Integer> {
    Optional<Vet> findByEmail(String email);
}
