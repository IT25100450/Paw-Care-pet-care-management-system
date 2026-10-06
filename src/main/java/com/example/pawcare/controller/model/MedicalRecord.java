package com.example.pawcare.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Medical_Records")
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MedicalRecordID")
    private Integer medicalRecordId;

    @Column(name = "Diagnosis", length = 255)
    private String diagnosis;

    @Column(name = "TreatmentProvided", length = 255)
    private String treatmentProvided;

    @Column(name = "FollowUpRecommendations", length = 255)
    private String followUpRecommendations;

    @Column(name = "Prescribed_Medication", length = 255)
    private String prescribedMedication;

    @OneToOne
    @JoinColumn(name = "Appointment_ID", nullable = false, unique = true)
    private VetAppointment appointment;

    public MedicalRecord() {}

    public Integer getMedicalRecordId() { return medicalRecordId; }
    public void setMedicalRecordId(Integer medicalRecordId) { this.medicalRecordId = medicalRecordId; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getTreatmentProvided() { return treatmentProvided; }
    public void setTreatmentProvided(String treatmentProvided) { this.treatmentProvided = treatmentProvided; }

    public String getFollowUpRecommendations() { return followUpRecommendations; }
    public void setFollowUpRecommendations(String followUpRecommendations) { this.followUpRecommendations = followUpRecommendations; }

    public String getPrescribedMedication() { return prescribedMedication; }
    public void setPrescribedMedication(String prescribedMedication) { this.prescribedMedication = prescribedMedication; }

    public VetAppointment getAppointment() { return appointment; }
    public void setAppointment(VetAppointment appointment) { this.appointment = appointment; }
}
