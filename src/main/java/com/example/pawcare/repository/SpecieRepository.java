package com.example.pawcare.repository;

import com.example.pawcare.model.Specie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecieRepository extends JpaRepository<Specie, Integer> {
}
