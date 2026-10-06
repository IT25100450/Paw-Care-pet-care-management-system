package com.example.pawcare.repository;

import com.example.pawcare.model.Breed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BreedRepository extends JpaRepository<Breed, Integer> {
    List<Breed> findBySpecieSpecieId(Integer specieId);
}
