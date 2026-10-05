-- =============================================
-- PawCare – Full Schema Rebuild
-- Target: Microsoft SQL Server
-- =============================================

-- Drop existing database and recreate
IF EXISTS (SELECT name FROM sys.databases WHERE name = 'PawCare')
BEGIN
    ALTER DATABASE PawCare SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE PawCare;
END
GO

CREATE DATABASE PawCare;
GO
USE PawCare;
GO

-- ── LOOKUP TABLES ──────────────────────────────────────────────────

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
    Role_Name VARCHAR(50) NOT NULL
);

CREATE TABLE Product_Category (
    Product_Category_ID INT IDENTITY PRIMARY KEY,
    Product_Category_Name VARCHAR(25) NOT NULL
);

-- ── PET OWNER / PET ────────────────────────────────────────────────

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

-- ── GROOMER ─────────────────────────────────────────────────────────

CREATE TABLE Groomer (
    Groomer_ID INT IDENTITY PRIMARY KEY,
    First_Name VARCHAR(50),
    Last_Name  VARCHAR(50),
    Email      VARCHAR(100) UNIQUE,
    Contact_No VARCHAR(20),
    Specialty  VARCHAR(100)
);

-- ── BOOKING (disjoint + Total specialization) ──────────────────────

CREATE TABLE Booking (
    Booking_ID INT IDENTITY PRIMARY KEY,
    Owner_ID INT NOT NULL,
    Pet_ID INT NOT NULL,
    Booking_DateTime DATETIME NOT NULL,
    Status VARCHAR(20) NOT NULL DEFAULT 'pending',
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
    Groomer_ID INT,
    Status VARCHAR(20) DEFAULT 'pending',
    CONSTRAINT fk_groomingbooking_booking FOREIGN KEY (Booking_ID) REFERENCES Booking (Booking_ID),
    CONSTRAINT fk_gb_groomer FOREIGN KEY (Groomer_ID) REFERENCES Groomer (Groomer_ID)
);

-- ── RECORDS (weak entities) ────────────────────────────────────────

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



-- ── STAFF (disjoint + total specialization) ────────────────────────

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

-- ── VET ────────────────────────────────────────────────────────────

CREATE TABLE Vet (
    Vet_ID INT IDENTITY PRIMARY KEY,
    First_Name VARCHAR(50),
    Last_Name  VARCHAR(50),
    Email      VARCHAR(100) UNIQUE,
    Contact_No VARCHAR(20),
    Specialization VARCHAR(100)
);

-- ── PRODUCT ────────────────────────────────────────────────────────

CREATE TABLE Product (
    Product_ID INT IDENTITY PRIMARY KEY,
    ProductName VARCHAR(45) NOT NULL,
    Quantity INT DEFAULT 0,
    Price DECIMAL (10,2) NOT NULL,
    Description VARCHAR(255),
    Image_URL VARCHAR(500),
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

-- ── VET APPOINTMENT / MEDICAL RECORDS ──────────────────────────────

CREATE TABLE Vet_Appointment (
    Appointment_ID INT IDENTITY PRIMARY KEY,
    Appointment_DateTime DATETIME NOT NULL,
    Reason VARCHAR(255),
    Status VARCHAR(20) NOT NULL DEFAULT 'pending',
    Pet_ID INT NOT NULL,
    Vet_ID INT,
    CONSTRAINT fk_vetapp_pet FOREIGN KEY (Pet_ID) REFERENCES Pet (Pet_ID),
    CONSTRAINT fk_vet_appointment_vet FOREIGN KEY (Vet_ID) REFERENCES Vet (Vet_ID)
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

-- ── ORDERS ─────────────────────────────────────────────────────────

CREATE TABLE Customer_Order (
    Order_ID   INT IDENTITY PRIMARY KEY,
    Owner_ID   INT NOT NULL,
    Order_Date DATETIME NOT NULL DEFAULT GETDATE(),
    Status     VARCHAR(20) NOT NULL DEFAULT 'confirmed',
    CONSTRAINT fk_order_owner FOREIGN KEY (Owner_ID) REFERENCES Pet_Owner (Owner_ID)
);

CREATE TABLE Order_Item (
    Order_Item_ID INT IDENTITY PRIMARY KEY,
    Order_ID      INT NOT NULL,
    Product_ID    INT NOT NULL,
    Quantity      INT NOT NULL DEFAULT 1,
    Unit_Price    DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_orderitem_order   FOREIGN KEY (Order_ID)   REFERENCES Customer_Order (Order_ID),
    CONSTRAINT fk_orderitem_product FOREIGN KEY (Product_ID) REFERENCES Product (Product_ID)
);

-- ── INQUIRY ────────────────────────────────────────────────────────

CREATE TABLE Inquiry (
    Inquiry_ID   INT IDENTITY PRIMARY KEY,
    Owner_ID     INT,
    Subject      VARCHAR(150),
    Message      VARCHAR(1000),
    Status       VARCHAR(20) DEFAULT 'Pending',
    Created_At   DATETIME DEFAULT GETDATE(),
    Response     VARCHAR(1000),
    Responded_By INT,
    CONSTRAINT fk_inquiry_owner FOREIGN KEY (Owner_ID) REFERENCES Pet_Owner(Owner_ID),
    CONSTRAINT fk_inquiry_staff FOREIGN KEY (Responded_By) REFERENCES Staff(Staff_ID)
);

-- ══════════════════════════════════════════════════════════════════
-- SEED DATA
-- ══════════════════════════════════════════════════════════════════

IF NOT EXISTS (SELECT 1 FROM Status)
BEGIN
    -- Status
    SET IDENTITY_INSERT Status ON;
    INSERT INTO Status (Status_ID, Status_Name) VALUES 
    (1, 'Pending'), (2, 'Confirmed'), (3, 'Checked_In'), (4, 'Checked_Out'), (5, 'Cancelled'), (6, 'Completed');
    SET IDENTITY_INSERT Status OFF;
END

IF NOT EXISTS (SELECT 1 FROM Specie)
BEGIN
    -- Species
    SET IDENTITY_INSERT Specie ON;
    INSERT INTO Specie (Specie_ID, Specie_name) VALUES 
    (1, 'Dog'), (2, 'Cat'), (3, 'Bird'), (4, 'Rabbit'), (5, 'Hamster');
    SET IDENTITY_INSERT Specie OFF;
END

IF NOT EXISTS (SELECT 1 FROM Breed)
BEGIN
    -- Breeds
    SET IDENTITY_INSERT Breed ON;
    INSERT INTO Breed (Breed_ID, Breed_name, Specie_ID) VALUES
    (1, 'Labrador Retriever', 1), (2, 'German Shepherd', 1), (3, 'Golden Retriever', 1), (4, 'Bulldog', 1), (5, 'Poodle', 1),
    (6, 'Persian', 2), (7, 'Siamese', 2), (8, 'Maine Coon', 2), (9, 'Bengal', 2),
    (10, 'Parakeet', 3), (11, 'Cockatiel', 3),
    (12, 'Holland Lop', 4), (13, 'Mini Rex', 4),
    (14, 'Syrian', 5), (15, 'Dwarf Campbell', 5);
    SET IDENTITY_INSERT Breed OFF;
END

IF NOT EXISTS (SELECT 1 FROM Roles)
BEGIN
    -- Roles
    SET IDENTITY_INSERT Roles ON;
    INSERT INTO Roles (Role_ID, Role_Name) VALUES
    (1, 'Operations Manager'),
    (2, 'Veterinary Coordinator'),
    (3, 'Boarding Services Manager'),
    (4, 'Grooming Centre Supervisor'),
    (5, 'Customer Care Supervisor');
    SET IDENTITY_INSERT Roles OFF;
END

IF NOT EXISTS (SELECT 1 FROM Product_Category)
BEGIN
    -- Product Categories
    SET IDENTITY_INSERT Product_Category ON;
    INSERT INTO Product_Category (Product_Category_ID, Product_Category_Name) VALUES
    (1, 'Food'), (2, 'Toys'), (3, 'Accessories'), (4, 'Healthcare'), (5, 'Grooming');
    SET IDENTITY_INSERT Product_Category OFF;
END

IF NOT EXISTS (SELECT 1 FROM Staff)
BEGIN
    -- Default Staff Accounts (password: Password123!)
    SET IDENTITY_INSERT Staff ON;
    INSERT INTO Staff (Staff_ID, Email, Password, Contact_No, Role_ID) VALUES
    (1, 'admin@pawcare.com', 'Password123!', '0771234567', 1),
    (2, 'vet@pawcare.com', 'Password123!', '0772345678', 2),
    (3, 'boarding@pawcare.com', 'Password123!', '0773456789', 3),
    (4, 'grooming@pawcare.com', 'Password123!', '0774567890', 4),
    (5, 'support@pawcare.com', 'Password123!', '0775678901', 5);
    SET IDENTITY_INSERT Staff OFF;

    -- Insert staff specialization rows (No identity column on these, they share PK with Staff)
    INSERT INTO Operations_Manager (Staff_ID) VALUES (1);
    INSERT INTO Veterinary_Coordinator (Staff_ID, LicenseNumber, Specialty) VALUES (2, 'VET-2024-001', 'General Practice');
    INSERT INTO Boarding_Services_Manager (Staff_ID) VALUES (3);
    INSERT INTO Grooming_Centre_Supervisor (Staff_ID) VALUES (4);
    INSERT INTO Customer_Care_Supervisor (Staff_ID) VALUES (5);
END

IF NOT EXISTS (SELECT 1 FROM Product)
BEGIN
    -- Sample Products
    SET IDENTITY_INSERT Product ON;
    INSERT INTO Product (Product_ID, ProductName, Quantity, Price, Description, Product_Category_ID) VALUES
    (1, 'Premium Dog Food 5kg', 50, 4500.00, 'High-quality dry dog food for all breeds', 1),
    (2, 'Cat Wet Food Pack', 100, 1200.00, 'Assorted flavors wet food for cats', 1),
    (3, 'Squeaky Ball Toy', 200, 350.00, 'Durable rubber ball with squeaker', 2),
    (4, 'Leather Dog Collar', 75, 1800.00, 'Adjustable genuine leather collar', 3),
    (5, 'Flea & Tick Shampoo', 60, 950.00, 'Medicated anti-parasite shampoo', 5);
    SET IDENTITY_INSERT Product OFF;
END

PRINT 'PawCare schema created and seeded successfully!';
GO
