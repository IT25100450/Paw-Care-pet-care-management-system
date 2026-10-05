package com.example.pawcare.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Vet_Appointment")
public class VetAppointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Appointment_ID")
    private Integer appointmentId;

    @Column(name = "Appointment_DateTime", nullable = false)
    private LocalDateTime appointmentDateTime;

    @Column(name = "Reason", length = 255)
    private String reason;

    @Column(name = "Status", length = 20)
    private String status = "pending";

    @ManyToOne
    @JoinColumn(name = "Pet_ID", nullable = false)
    private Pet pet;

    @ManyToOne
    @JoinColumn(name = "Vet_ID", nullable = true)  // nullable — assigned by coordinator
    private Vet vet;

    public VetAppointment() {}

    public Integer getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Integer appointmentId) { this.appointmentId = appointmentId; }

    public LocalDateTime getAppointmentDateTime() { return appointmentDateTime; }
    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) { this.appointmentDateTime = appointmentDateTime; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Pet getPet() { return pet; }
    public void setPet(Pet pet) { this.pet = pet; }

    public Vet getVet() { return vet; }
    public void setVet(Vet vet) { this.vet = vet; }
}
