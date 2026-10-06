package com.example.pawcare.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Medical_Record_Vaccination")
@IdClass(MedicalRecordVaccinationId.class)
public class MedicalRecordVaccination {

    @Id
    @ManyToOne
    @JoinColumn(name = "MedicalRecordID", nullable = false)
    private MedicalRecord medicalRecord;

    @Id
    @Column(name = "Type_V", length = 100)
    private String typeV;

    @Id
    @Column(name = "DateAdministered")
    private LocalDate dateAdministered;

    @Id
    @Column(name = "NextDueDate")
    private LocalDate nextDueDate;

    public MedicalRecordVaccination() {}

    public MedicalRecord getMedicalRecord() { return medicalRecord; }
    public void setMedicalRecord(MedicalRecord medicalRecord) { this.medicalRecord = medicalRecord; }

    public String getTypeV() { return typeV; }
    public void setTypeV(String typeV) { this.typeV = typeV; }

    public LocalDate getDateAdministered() { return dateAdministered; }
    public void setDateAdministered(LocalDate dateAdministered) { this.dateAdministered = dateAdministered; }

    public LocalDate getNextDueDate() { return nextDueDate; }
    public void setNextDueDate(LocalDate nextDueDate) { this.nextDueDate = nextDueDate; }
}
