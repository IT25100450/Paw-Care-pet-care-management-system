package com.example.pawcare.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class MedicalRecordVaccinationId implements Serializable {
    private Integer medicalRecord;
    private String typeV;
    private LocalDate dateAdministered;
    private LocalDate nextDueDate;

    public MedicalRecordVaccinationId() {}

    public MedicalRecordVaccinationId(Integer medicalRecord, String typeV, LocalDate dateAdministered, LocalDate nextDueDate) {
        this.medicalRecord = medicalRecord;
        this.typeV = typeV;
        this.dateAdministered = dateAdministered;
        this.nextDueDate = nextDueDate;
    }

    public Integer getMedicalRecord() { return medicalRecord; }
    public void setMedicalRecord(Integer medicalRecord) { this.medicalRecord = medicalRecord; }

    public String getTypeV() { return typeV; }
    public void setTypeV(String typeV) { this.typeV = typeV; }

    public LocalDate getDateAdministered() { return dateAdministered; }
    public void setDateAdministered(LocalDate dateAdministered) { this.dateAdministered = dateAdministered; }

    public LocalDate getNextDueDate() { return nextDueDate; }
    public void setNextDueDate(LocalDate nextDueDate) { this.nextDueDate = nextDueDate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MedicalRecordVaccinationId that = (MedicalRecordVaccinationId) o;
        return Objects.equals(medicalRecord, that.medicalRecord) &&
               Objects.equals(typeV, that.typeV) &&
               Objects.equals(dateAdministered, that.dateAdministered) &&
               Objects.equals(nextDueDate, that.nextDueDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(medicalRecord, typeV, dateAdministered, nextDueDate);
    }
}
