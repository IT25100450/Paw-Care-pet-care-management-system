package com.example.pawcare.repository;

import com.example.pawcare.model.PetOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PetOwnerRepository extends JpaRepository<PetOwner, Integer> {
    Optional<PetOwner> findByEmail(String email);
    Optional<PetOwner> findByEmailAndPassword(String email, String password);
}
