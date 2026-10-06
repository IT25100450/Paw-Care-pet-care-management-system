package com.example.pawcare.repository;

import com.example.pawcare.model.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Integer> {

    @Query("SELECT m FROM MedicalRecord m JOIN FETCH m.appointment a JOIN FETCH a.pet p JOIN FETCH p.owner")
    List<MedicalRecord> findAllWithDetails();

    Optional<MedicalRecord> findByAppointmentAppointmentId(Integer appointmentId);

    default Optional<MedicalRecord> findByAppointmentId(Integer appointmentId) {
        return findByAppointmentAppointmentId(appointmentId);
    }
}
