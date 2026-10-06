package com.example.pawcare.repository;

import com.example.pawcare.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    @Query("SELECT b FROM Booking b JOIN FETCH b.owner JOIN FETCH b.pet")
    List<Booking> findAllWithDetails();

    List<Booking> findByOwnerOwnerId(Integer ownerId);
    List<Booking> findByPetPetId(Integer petId);
}
