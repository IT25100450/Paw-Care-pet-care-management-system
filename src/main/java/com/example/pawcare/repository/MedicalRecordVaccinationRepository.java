package com.example.pawcare.repository;

import com.example.pawcare.model.MedicalRecordVaccination;
import com.example.pawcare.model.MedicalRecordVaccinationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalRecordVaccinationRepository extends JpaRepository<MedicalRecordVaccination, MedicalRecordVaccinationId> {
    List<MedicalRecordVaccination> findByMedicalRecord_MedicalRecordId(Integer medicalRecordId);
    void deleteByMedicalRecord_MedicalRecordId(Integer medicalRecordId);
}
