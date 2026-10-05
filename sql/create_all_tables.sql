-- ═══════════════════════════════════════════════════════════════════
-- PawCare – Complete Normalized Database Script (MS SQL Server)
-- Run this in SSMS against database: pawcare_db
-- ═══════════════════════════════════════════════════════════════════

USE pawcare_db;
GO

-- ───────────────────────────────────────────────────────────────────
-- 1. OWNERS (Pet Owners / Customers)
-- ───────────────────────────────────────────────────────────────────
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Owners')
BEGIN
    CREATE TABLE Owners (
        Owner_ID    INT IDENTITY(1,1) PRIMARY KEY,
        Name        NVARCHAR(100) NOT NULL,
        Email       NVARCHAR(100) NOT NULL UNIQUE,
        Phone       NVARCHAR(20)  NULL
    );
END
GO

-- ───────────────────────────────────────────────────────────────────
-- 2. PETS (belongs to an Owner)
-- ───────────────────────────────────────────────────────────────────
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Pets')
BEGIN
    CREATE TABLE Pets (
        Pet_ID      INT IDENTITY(1,1) PRIMARY KEY,
        Owner_ID    INT NOT NULL,
        Name        NVARCHAR(50) NOT NULL,
        CONSTRAINT FK_Pets_Owner
            FOREIGN KEY (Owner_ID) REFERENCES Owners(Owner_ID)
            ON DELETE CASCADE
    );
    CREATE INDEX IX_Pets_Owner ON Pets(Owner_ID);
END
GO

-- ───────────────────────────────────────────────────────────────────
-- 3. SERVICES (Service catalog – Grooming, Vet, etc.)
-- ───────────────────────────────────────────────────────────────────
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Services')
BEGIN
    CREATE TABLE Services (
        Service_ID   INT IDENTITY(1,1) PRIMARY KEY,
        Service_Name NVARCHAR(100) NOT NULL,
        Price        DECIMAL(10,2) NOT NULL DEFAULT 0
    );
END
GO

-- ───────────────────────────────────────────────────────────────────
-- 4. BOOKINGS (Appointment reservations)
-- ───────────────────────────────────────────────────────────────────
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Bookings')
BEGIN
    CREATE TABLE Bookings (
        Booking_ID  NVARCHAR(20)  PRIMARY KEY,
        Pet_ID      INT           NOT NULL,
        Service_ID  INT           NOT NULL,
        Start_Time  DATETIME2     NULL,
        End_Time    DATETIME2     NULL,
        Notes       NVARCHAR(MAX) NULL,
        Status      NVARCHAR(20)  DEFAULT 'Pending',
        CONSTRAINT FK_Bookings_Pet
            FOREIGN KEY (Pet_ID)     REFERENCES Pets(Pet_ID),
        CONSTRAINT FK_Bookings_Service
            FOREIGN KEY (Service_ID) REFERENCES Services(Service_ID)
    );
    CREATE INDEX IX_Bookings_Pet     ON Bookings(Pet_ID);
    CREATE INDEX IX_Bookings_Service ON Bookings(Service_ID);
    CREATE INDEX IX_Bookings_Status  ON Bookings(Status);
END
GO

-- ───────────────────────────────────────────────────────────────────
-- 5. MEDICAL RECORDS (Veterinary checkup records)
-- ───────────────────────────────────────────────────────────────────
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Medical_Records')
BEGIN
    CREATE TABLE Medical_Records (
        Record_ID        INT IDENTITY(1,1) PRIMARY KEY,
        Pet_ID           INT           NOT NULL,
        Diagnosis        NVARCHAR(MAX) NULL,
        Treatment        NVARCHAR(MAX) NULL,
        Vaccination_Date DATE          NULL,
        Next_Due_Date    DATE          NULL,
        Vet_Staff        NVARCHAR(100) NULL,
        Notes            NVARCHAR(MAX) NULL,
        Record_Date      DATETIME2     NULL DEFAULT GETDATE(),
        CONSTRAINT FK_Medical_Pet
            FOREIGN KEY (Pet_ID) REFERENCES Pets(Pet_ID)
            ON DELETE CASCADE
    );
    CREATE INDEX IX_Medical_Pet     ON Medical_Records(Pet_ID);
    CREATE INDEX IX_Medical_NextDue ON Medical_Records(Next_Due_Date);
END
GO

-- ───────────────────────────────────────────────────────────────────
-- 6. GROOMING LOGS (Grooming session records)
-- ───────────────────────────────────────────────────────────────────
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Grooming_Logs')
BEGIN
    CREATE TABLE Grooming_Logs (
        Log_ID          INT IDENTITY(1,1) PRIMARY KEY,
        Pet_ID          INT           NOT NULL,
        Booking_ID      NVARCHAR(20)  NULL,
        Service_Type    NVARCHAR(100) NULL,
        Products_Used   NVARCHAR(MAX) NULL,
        Coat_Condition  NVARCHAR(100) NULL,
        Skin_Notes      NVARCHAR(MAX) NULL,
        Groomer         NVARCHAR(100) NULL,
        Session_Date    DATETIME2     NULL DEFAULT GETDATE(),
        CONSTRAINT FK_Grooming_Pet
            FOREIGN KEY (Pet_ID)     REFERENCES Pets(Pet_ID)
            ON DELETE CASCADE,
        CONSTRAINT FK_Grooming_Booking
            FOREIGN KEY (Booking_ID) REFERENCES Bookings(Booking_ID)
            ON DELETE SET NULL
    );
    CREATE INDEX IX_Grooming_Pet ON Grooming_Logs(Pet_ID);
END
GO

-- ───────────────────────────────────────────────────────────────────
-- 7. KENNELS (Boarding kennel definitions)
-- ───────────────────────────────────────────────────────────────────
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Kennels')
BEGIN
    CREATE TABLE Kennels (
        Kennel_ID     INT IDENTITY(1,1) PRIMARY KEY,
        Kennel_Number NVARCHAR(20)  NOT NULL UNIQUE,
        Size          NVARCHAR(20)  NULL DEFAULT 'MEDIUM',
        Status        NVARCHAR(20)  NULL DEFAULT 'AVAILABLE'
    );
END
GO

-- ───────────────────────────────────────────────────────────────────
-- 8. BOARDING RECORDS (Check-in / Check-out tracking)
-- ───────────────────────────────────────────────────────────────────
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Boarding_Records')
BEGIN
    CREATE TABLE Boarding_Records (
        Boarding_ID          INT IDENTITY(1,1) PRIMARY KEY,
        Pet_ID               INT           NOT NULL,
        Kennel_ID            INT           NOT NULL,
        Check_In_Time        DATETIME2     NULL,
        Check_Out_Time       DATETIME2     NULL,
        Status               NVARCHAR(20)  NULL DEFAULT 'RESERVED',
        Diet_Notes           NVARCHAR(MAX) NULL,
        Special_Instructions NVARCHAR(MAX) NULL,
        CONSTRAINT FK_Boarding_Pet
            FOREIGN KEY (Pet_ID)    REFERENCES Pets(Pet_ID)
            ON DELETE CASCADE,
        CONSTRAINT FK_Boarding_Kennel
            FOREIGN KEY (Kennel_ID) REFERENCES Kennels(Kennel_ID)
    );
    CREATE INDEX IX_Boarding_Pet    ON Boarding_Records(Pet_ID);
    CREATE INDEX IX_Boarding_Kennel ON Boarding_Records(Kennel_ID);
    CREATE INDEX IX_Boarding_Status ON Boarding_Records(Status);
END
GO

-- ───────────────────────────────────────────────────────────────────
-- 9. PRODUCTS (Pet Store catalog)
-- ───────────────────────────────────────────────────────────────────
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Products')
BEGIN
    CREATE TABLE Products (
        Product_ID   INT IDENTITY(1,1) PRIMARY KEY,
        Name         NVARCHAR(150)  NOT NULL,
        Category     NVARCHAR(50)   NULL,
        Price        DECIMAL(10,2)  NOT NULL DEFAULT 0,
        Stock_Qty    INT            NULL DEFAULT 0,
        Description  NVARCHAR(MAX)  NULL,
        Image_Url    NVARCHAR(500)  NULL
    );
    CREATE INDEX IX_Products_Category ON Products(Category);
END
GO

-- ═══════════════════════════════════════════════════════════════════
-- OPTIONAL: Seed sample kennels (uncomment to run)
-- ═══════════════════════════════════════════════════════════════════
/*
INSERT INTO Kennels (Kennel_Number, Size, Status) VALUES
    ('K-01', 'SMALL',  'AVAILABLE'),
    ('K-02', 'SMALL',  'AVAILABLE'),
    ('K-03', 'MEDIUM', 'AVAILABLE'),
    ('K-04', 'MEDIUM', 'AVAILABLE'),
    ('K-05', 'MEDIUM', 'AVAILABLE'),
    ('K-06', 'LARGE',  'AVAILABLE'),
    ('K-07', 'LARGE',  'AVAILABLE'),
    ('K-08', 'LARGE',  'AVAILABLE');
*/

-- ═══════════════════════════════════════════════════════════════════
-- OPTIONAL: Seed sample products (uncomment to run)
-- ═══════════════════════════════════════════════════════════════════
/*
INSERT INTO Products (Name, Category, Price, Stock_Qty, Description) VALUES
    ('Premium Dry Dog Food 5kg',  'Food',        2500, 30, 'High-protein kibble for adult dogs'),
    ('Cat Wet Food Tuna 12-pack', 'Food',        1800, 45, 'Premium wet food with real tuna'),
    ('Interactive Ball Launcher',  'Toys',        3200, 15, 'Automatic ball launcher for dogs'),
    ('Catnip Mouse Toy Set',      'Toys',         650, 60, 'Set of 5 catnip-infused mouse toys'),
    ('Adjustable Dog Harness',    'Accessories',  1500, 25, 'Comfortable padded harness, S/M/L sizes'),
    ('Ceramic Pet Water Fountain', 'Accessories', 4500, 10, 'Filtered circulating water fountain'),
    ('Flea and Tick Shampoo 500ml','Health',      1200, 40, 'Medicated shampoo for flea prevention'),
    ('Pet First Aid Kit',         'Health',       2800,  8, 'Complete first aid kit for pets');
*/

PRINT 'PawCare database schema created successfully.';
GO
