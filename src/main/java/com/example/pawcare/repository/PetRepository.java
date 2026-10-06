package com.example.pawcare.repository;

import com.example.pawcare.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PetRepository extends JpaRepository<Pet, Integer> {
    Optional<Pet> findByNameAndOwnerOwnerId(String name, Integer ownerId);
    List<Pet> findByOwnerOwnerId(Integer ownerId);

    @Query("SELECT p FROM Pet p JOIN FETCH p.owner JOIN FETCH p.specie")
    List<Pet> findAllWithDetails();
}
