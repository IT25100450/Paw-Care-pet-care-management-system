USE PawCare;
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Groomer')
BEGIN
    CREATE TABLE Groomer (
        Groomer_ID INT IDENTITY PRIMARY KEY,
        First_Name VARCHAR(50),
        Last_Name  VARCHAR(50),
        Email      VARCHAR(100) UNIQUE,
        Contact_No VARCHAR(20),
        Specialty  VARCHAR(100)
    );
    PRINT 'Created Groomer table';
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = object_id('Grooming_Booking') AND name = 'Groomer_ID')
BEGIN
    ALTER TABLE Grooming_Booking ADD Groomer_ID INT;
    ALTER TABLE Grooming_Booking ADD CONSTRAINT fk_gb_groomer FOREIGN KEY (Groomer_ID) REFERENCES Groomer(Groomer_ID);
    PRINT 'Added Groomer_ID to Grooming_Booking';
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = object_id('Grooming_Booking') AND name = 'Status')
BEGIN
    ALTER TABLE Grooming_Booking ADD Status VARCHAR(20) DEFAULT 'pending';
    PRINT 'Added Status to Grooming_Booking';
END
GO
