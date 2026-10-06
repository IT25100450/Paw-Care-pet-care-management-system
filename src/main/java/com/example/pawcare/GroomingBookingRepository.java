package com.example.pawcare.repository;

import com.example.pawcare.model.GroomingBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GroomingBookingRepository extends JpaRepository<GroomingBooking, Integer> {

    @Query("SELECT gb FROM GroomingBooking gb JOIN FETCH gb.booking b JOIN FETCH b.owner JOIN FETCH b.pet")
    List<GroomingBooking> findAllWithDetails();

    /** Confirmed sessions of a groomer whose start time falls inside [from, to], excluding one booking. */
    @Query("SELECT gb FROM GroomingBooking gb JOIN gb.booking b " +
           "WHERE gb.groomer.groomerId = :groomerId AND gb.bookingId <> :excludeId " +
           "AND LOWER(gb.status) = 'confirmed' " +
           "AND b.bookingDateTime > :from AND b.bookingDateTime < :to")
    List<GroomingBooking> findGroomerConflicts(@Param("groomerId") Integer groomerId,
                                               @Param("excludeId") Integer excludeId,
                                               @Param("from") LocalDateTime from,
                                               @Param("to") LocalDateTime to);
}
