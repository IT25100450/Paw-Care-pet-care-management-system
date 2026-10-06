package com.example.pawcare.repository;

import com.example.pawcare.model.VetAppointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VetAppointmentRepository extends JpaRepository<VetAppointment, Integer> {

    // Vet is LEFT JOIN because it may be null (pending, unassigned)
    @Query("SELECT va FROM VetAppointment va JOIN FETCH va.pet p JOIN FETCH p.owner LEFT JOIN FETCH va.vet")
    List<VetAppointment> findAllWithDetails();

    @Query("SELECT va FROM VetAppointment va JOIN FETCH va.pet p JOIN FETCH p.owner LEFT JOIN FETCH va.vet WHERE p.owner.ownerId = :ownerId")
    List<VetAppointment> findByPetOwnerOwnerId(Integer ownerId);

    List<VetAppointment> findByPetPetId(Integer petId);

    List<VetAppointment> findByAppointmentDateTimeBetween(LocalDateTime start, LocalDateTime end);
}
