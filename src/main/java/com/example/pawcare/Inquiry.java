package com.example.pawcare.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Inquiry")
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Inquiry_ID")
    private Integer inquiryId;

    @ManyToOne
    @JoinColumn(name = "Owner_ID")
    private PetOwner owner;

    @Column(name = "Subject", length = 150)
    private String subject;

    @Column(name = "Message", length = 1000)
    private String message;

    @Column(name = "Status", length = 20)
    private String status = "Pending";

    @Column(name = "Created_At")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "Response", length = 1000)
    private String response;

    @ManyToOne
    @JoinColumn(name = "Responded_By")
    private Staff respondedBy;

    public Inquiry() {}

    public Integer getInquiryId() { return inquiryId; }
    public void setInquiryId(Integer inquiryId) { this.inquiryId = inquiryId; }

    public PetOwner getOwner() { return owner; }
    public void setOwner(PetOwner owner) { this.owner = owner; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public Staff getRespondedBy() { return respondedBy; }
    public void setRespondedBy(Staff respondedBy) { this.respondedBy = respondedBy; }
}
