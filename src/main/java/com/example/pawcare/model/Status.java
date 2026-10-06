package com.example.pawcare.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Status")
public class Status {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Status_ID")
    private Integer statusId;

    @Column(name = "Status_Name", nullable = false, length = 15)
    private String statusName;

    public Status() {}

    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer statusId) { this.statusId = statusId; }

    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }
}
