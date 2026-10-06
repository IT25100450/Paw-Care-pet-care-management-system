package com.example.pawcare.repository;

import com.example.pawcare.model.Groomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface GroomerRepository extends JpaRepository<Groomer, Integer> {
    Optional<Groomer> findByEmail(String email);
}
