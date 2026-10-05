# PawCare — Issues & Requirements

This document consolidates the reported issues, database changes, and role-access requirements for the PawCare system.

## 1. Access Flow Issue
Login/Signup is not gating access correctly. The **index page** should be the only page visible to an unauthenticated visitor. Booking and service pages should only become reachable **after** the person logs in.

## 2. Customer Profile Not Populating
When a customer books boarding, the booking should appear under that customer's **user profile**. Currently the profile shows nothing — booking records aren't being linked to / displayed for the logged-in owner.

## 3. Incorrect Page Routing
Every appointment/booking type currently redirects to the generic **booking page**, regardless of service. Each service must route to its own dedicated page:
- Boarding → Boarding Booking page
- Grooming → Grooming Booking page
- Veterinary → Veterinary Appointment page
- (and so on for any other service)

## 4. Admin/Staff Separation
The admin side must be **completely separate** from the customer-facing side. Each authorized staff role should only be able to see and manage the service(s) assigned to their role — not the whole system.

Additionally: since a customer is already logged in, their **name, contact number, and email** should be auto-filled from their account wherever needed (they shouldn't have to re-enter this information on booking/appointment forms).

## 5. Database Schema Update
The database should be rebuilt to match the schema below (SQL Server / T-SQL syntax).

```sql
CREATE DATABASE Pawcare;
GO
USE PawCare;
GO

CREATE TABLE Status (
    Status_ID INT IDENTITY PRIMARY KEY,
    Status_Name VARCHAR(15) NOT NULL
);

CREATE TABLE Specie (
    Specie_ID INT IDENTITY PRIMARY KEY,
    Specie_name VARCHAR(50) NOT NULL
);

CREATE TABLE Breed (
    Breed_ID INT IDENTITY PRIMARY KEY,
    Breed_name VARCHAR(50) NOT NULL,
    Specie_ID INT NOT NULL,
    CONSTRAINT fk_breed_specie FOREIGN KEY (Specie_ID) REFERENCES Specie (Specie_ID)
);

CREATE TABLE Roles (
    Role_ID INT IDENTITY PRIMARY KEY,
    Role_Name VARCHAR(25) NOT NULL
);

CREATE TABLE Product_Category (
    Product_Category_ID INT IDENTITY PRIMARY KEY,
    Product_Category_Name VARCHAR(25) NOT NULL
);

-- Pet Owner / Pet
CREATE TABLE Pet_Owner (
    Owner_ID INT IDENTITY PRIMARY KEY,
    First_name VARCHAR(50) NOT NULL,
    Last_name VARCHAR(50) NOT NULL,
    Email VARCHAR(100) NOT NULL UNIQUE,
    Password VARCHAR(255) NOT NULL,
    Contact_No VARCHAR(10) NOT NULL,
    House_No VARCHAR(20),
    Street_name VARCHAR(30),
    City VARCHAR(25),
    Province VARCHAR(25),
    Postal_code VARCHAR(20)
);

CREATE TABLE Pet (
    Pet_ID INT IDENTITY PRIMARY KEY,
    Name VARCHAR(50) NOT NULL,
    DateOfBirth DATE,
    Owner_ID INT NOT NULL,
    Specie_ID INT NOT NULL,
    CONSTRAINT uq_pet_owner UNIQUE (Pet_ID, Owner_ID),
    CONSTRAINT fk_pet_owner FOREIGN KEY (Owner_ID) REFERENCES Pet_Owner(Owner_ID),
    CONSTRAINT fk_pet_specie FOREIGN KEY (Specie_ID) REFERENCES Specie (Specie_ID)
);

CREATE TABLE Pet_Preferences(
    Pet_ID INT NOT NULL,
    Preferences VARCHAR(50),
    PRIMARY KEY (Pet_ID, Preferences),
    CONSTRAINT fk_pet_pref FOREIGN KEY (Pet_ID) REFERENCES Pet(Pet_ID)
);

CREATE TABLE Pet_Medical_Allergies(
    Pet_ID INT NOT NULL,
    Medical_Allergies VARCHAR(50),
    PRIMARY KEY (Pet_ID, Medical_Allergies),
    CONSTRAINT fk_pet_med_allergies FOREIGN KEY (Pet_ID) REFERENCES Pet(Pet_ID)
);

-- Booking (disjoint + Total specialization)
CREATE TABLE Booking (
    Booking_ID INT IDENTITY PRIMARY KEY,
    Owner_ID INT NOT NULL,
    Pet_ID INT NOT NULL,
    Booking_DateTime DATETIME NOT NULL,
    CONSTRAINT fk_booking_owner FOREIGN KEY (Owner_ID) REFERENCES Pet_Owner (Owner_ID),
    CONSTRAINT fk_booking_pet_owner FOREIGN KEY (Pet_ID, Owner_ID) REFERENCES Pet (Pet_ID, Owner_ID)
);

CREATE TABLE Boarding_Booking (
    Booking_ID INT PRIMARY KEY,
    CheckoutDate DATE,
    Kennel_No VARCHAR(20),
    CONSTRAINT fk_boardingbooking_booking FOREIGN KEY (Booking_ID) REFERENCES Booking (Booking_ID)
);

CREATE TABLE Boarding_Booking_scInstructions(
    Booking_ID INT NOT NULL,
    SpecialCareInstructions VARCHAR(255),
    PRIMARY KEY (Booking_ID, SpecialCareInstructions),
    CONSTRAINT fk_Boarding_Booking_scInstructions FOREIGN KEY (Booking_ID) REFERENCES Boarding_Booking (Booking_ID)
);

CREATE TABLE Grooming_Booking (
    Booking_ID INT PRIMARY KEY,
    Type VARCHAR(25),
    CONSTRAINT fk_groomingbooking_booking FOREIGN KEY (Booking_ID) REFERENCES Booking (Booking_ID)
);

-- Weak entities identified via "Records" relationships
CREATE TABLE Boarding_Record (
    B_record_ID INT NOT NULL,
    Booking_ID INT NOT NULL,
    Status_ID INT,
    PRIMARY KEY (Booking_ID, B_record_ID),
    CONSTRAINT fk_boardingrecord_booking FOREIGN KEY (Booking_ID) REFERENCES Boarding_Booking (Booking_ID),
    CONSTRAINT fk_boardingrecord_status FOREIGN KEY (Status_ID) REFERENCES Status (Status_ID)
);

CREATE TABLE Grooming_Record (
    G_record_ID INT NOT NULL,
    Booking_ID INT NOT NULL,
    IssuesIdentified VARCHAR(255),
    Recommendations VARCHAR(255),
    PRIMARY KEY (Booking_ID, G_record_ID),
    CONSTRAINT fk_groomingrecord_booking FOREIGN KEY (Booking_ID) REFERENCES Grooming_Booking (Booking_ID)
);

CREATE TABLE Grooming_Record_Services(
    Booking_ID INT NOT NULL,
    G_record_ID INT NOT NULL,
    G_Services VARCHAR(255),
    PRIMARY KEY (Booking_ID, G_record_ID, G_Services),
    CONSTRAINT fk_Grooming_Record_Services FOREIGN KEY (Booking_ID, G_record_ID) REFERENCES Grooming_Record (Booking_ID, G_record_ID)
);

CREATE TABLE Grooming_Record_ProductsUsed(
    Booking_ID INT NOT NULL,
    G_record_ID INT NOT NULL,
    ProductsUsed VARCHAR(45),
    PRIMARY KEY (Booking_ID, G_record_ID, ProductsUsed),
    CONSTRAINT fk_Grooming_Record_ProductsUsed FOREIGN KEY (Booking_ID, G_record_ID) REFERENCES Grooming_Record (Booking_ID, G_record_ID)
);

-- Staff (disjoint + total specialization)
CREATE TABLE Staff (
    Staff_ID INT IDENTITY PRIMARY KEY,
    Email VARCHAR(100) NOT NULL UNIQUE,
    Password VARCHAR(255) NOT NULL,
    Contact_No VARCHAR(10),
    Role_ID INT NOT NULL,
    CONSTRAINT fk_staff_role FOREIGN KEY (Role_ID) REFERENCES Roles (Role_ID)
);

CREATE TABLE Grooming_Centre_Supervisor (
    Staff_ID INT PRIMARY KEY,
    CONSTRAINT fk_gcs_staff FOREIGN KEY (Staff_ID) REFERENCES Staff(Staff_ID)
);

CREATE TABLE Boarding_Services_Manager (
    Staff_ID INT PRIMARY KEY,
    CONSTRAINT fk_bsm_staff FOREIGN KEY (Staff_ID) REFERENCES Staff(Staff_ID)
);

CREATE TABLE Operations_Manager (
    Staff_ID INT PRIMARY KEY,
    CONSTRAINT fk_om_staff FOREIGN KEY (Staff_ID) REFERENCES Staff (Staff_ID)
);

CREATE TABLE Customer_Care_Supervisor (
    Staff_ID INT PRIMARY KEY,
    CONSTRAINT fk_ccs_staff FOREIGN KEY (Staff_ID) REFERENCES Staff (Staff_ID)
);

CREATE TABLE Veterinary_Coordinator (
    Staff_ID INT PRIMARY KEY,
    LicenseNumber VARCHAR(50),
    Specialty VARCHAR(100),
    CONSTRAINT fk_vc_staff FOREIGN KEY (Staff_ID) REFERENCES Staff (Staff_ID)
);

-- Product / Product Category / Manages (M:N)
CREATE TABLE Product (
    Product_ID INT IDENTITY PRIMARY KEY,
    ProductName VARCHAR(45) NOT NULL,
    Quantity INT DEFAULT 0,
    Price DECIMAL (10,2) NOT NULL,
    Description VARCHAR(255),
    Product_Category_ID INT NOT NULL,
    CONSTRAINT fk_product_category FOREIGN KEY (Product_Category_ID) REFERENCES Product_Category (Product_Category_ID)
);

CREATE TABLE Manage_Product (
    Staff_ID INT NOT NULL,
    Product_ID INT NOT NULL,
    PRIMARY KEY (Staff_ID, Product_ID),
    CONSTRAINT fk_staff_manages FOREIGN KEY (Staff_ID) REFERENCES Staff(Staff_ID),
    CONSTRAINT fk_manages_product FOREIGN KEY (Product_ID) REFERENCES Product (Product_ID)
);

-- Vet Appointment / Medical Records / Vaccination
CREATE TABLE Vet_Appointment (
    Appointment_ID INT IDENTITY PRIMARY KEY,
    Appointment_DateTime DATETIME NOT NULL,
    Reason VARCHAR(255),
    Pet_ID INT NOT NULL,
    Staff_ID INT NOT NULL,
    CONSTRAINT fk_vetapp_pet FOREIGN KEY (Pet_ID) REFERENCES Pet (Pet_ID),
    CONSTRAINT fk_vetapp_coordinator FOREIGN KEY (Staff_ID) REFERENCES Veterinary_Coordinator (Staff_ID)
);

CREATE TABLE Medical_Records (
    MedicalRecordID INT IDENTITY PRIMARY KEY,
    Diagnosis VARCHAR(255),
    TreatmentProvided VARCHAR(255),
    FollowUpRecommendations VARCHAR(255),
    Prescribed_Medication VARCHAR(255),
    Appointment_ID INT NOT NULL UNIQUE,
    CONSTRAINT fk_medrecord_appointment FOREIGN KEY (Appointment_ID) REFERENCES Vet_Appointment (Appointment_ID)
);

CREATE TABLE Medical_Record_Vaccination(
    MedicalRecordID INT NOT NULL,
    Type_V VARCHAR(100),
    DateAdministered DATE,
    NextDueDate DATE,
    PRIMARY KEY(MedicalRecordID, Type_V, DateAdministered, NextDueDate),
    CONSTRAINT fk_vaccination_medrecord FOREIGN KEY (MedicalRecordID) REFERENCES Medical_Records (MedicalRecordID)
);
```

## 6. Cascade the Schema Change
Per item 5, the backend queries, models/entities, and every relevant **web page input field** must be updated to match this new schema (field names, new tables like `Breed`, `Specie`, `Pet_Preferences`, `Pet_Medical_Allergies`, the `Status`-driven boarding records, etc.).

## 7. Stakeholders & Role-Based Page Access
There are **seven** stakeholders. Each should only be able to see and act on the pages relevant to their own responsibilities:

| Role | Responsibilities |
|---|---|
| **Customer (Pet Owner)** | Registers pets, books services, views their pet's care history |
| **Operations Manager (Admin)** | Oversees overall system operation, manages users/staff, handles system-wide records |
| **Veterinary Coordinator** | Manages veterinary consultations, vaccination records, health check-up records |
| **Boarding Services Manager** | Manages boarding activities: check-ins, check-outs, boarding status updates |
| **Grooming Centre Supervisor** | Manages grooming bookings and grooming session records |
| **Customer Care Supervisor** | Handles customer support, resolves scheduling conflicts, manages pet store products |

> **Note:** Create additional web pages only if genuinely needed to satisfy the above — but do not build them silently. Any new page must first be flagged and described in the implementation plan (item 9) so it can be reviewed before work starts.

## 8. Clarification Rule
Before implementing any of the above, ask **yes/no clarifying questions** for any area that is ambiguous or underspecified, rather than assuming.

## 9. Implementation Plan Rule
Before starting/completing the actual implementation, provide a full **implementation plan** covering the proposed approach for every item above (including any new pages), so it can be reviewed and explicitly accepted or rejected before work begins.
