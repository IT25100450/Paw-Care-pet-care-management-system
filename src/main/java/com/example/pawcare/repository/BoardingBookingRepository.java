package com.example.pawcare.repository;

import com.example.pawcare.model.BoardingBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BoardingBookingRepository extends JpaRepository<BoardingBooking, Integer> {

    @Query("SELECT bb FROM BoardingBooking bb JOIN FETCH bb.booking b JOIN FETCH b.owner JOIN FETCH b.pet")
    List<BoardingBooking> findAllWithDetails();
}
