-- =================================================================================
-- PawCare Migration v4
-- Purpose: Create Vet table and update Vet_Appointment to use Vet_ID instead of Staff_ID
-- =================================================================================

USE PawCare;
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Vet')
BEGIN
    CREATE TABLE Vet (
        Vet_ID INT IDENTITY PRIMARY KEY,
        First_Name VARCHAR(50),
        Last_Name VARCHAR(50),
        Email VARCHAR(100) UNIQUE,
        Contact_No VARCHAR(20),
        Specialization VARCHAR(100)
    );
    PRINT 'Created Vet table';
END
GO

-- Drop foreign key from Vet_Appointment to Staff (if exists, named loosely or specifically)
-- We need to find the FK name dynamically in SQL Server
DECLARE @fkName VARCHAR(200);
SELECT @fkName = name FROM sys.foreign_keys WHERE parent_object_id = object_id('Vet_Appointment') AND referenced_object_id = object_id('Staff');
IF @fkName IS NOT NULL
BEGIN
    DECLARE @sql VARCHAR(MAX) = 'ALTER TABLE Vet_Appointment DROP CONSTRAINT ' + @fkName;
    EXEC(@sql);
END
GO

IF EXISTS (SELECT * FROM sys.columns WHERE object_id = object_id('Vet_Appointment') AND name = 'Staff_ID')
BEGIN
    ALTER TABLE Vet_Appointment DROP COLUMN Staff_ID;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = object_id('Vet_Appointment') AND name = 'Vet_ID')
BEGIN
    ALTER TABLE Vet_Appointment ADD Vet_ID INT;
    ALTER TABLE Vet_Appointment ADD CONSTRAINT fk_vet_appointment_vet FOREIGN KEY (Vet_ID) REFERENCES Vet(Vet_ID);
    PRINT 'Added Vet_ID to Vet_Appointment';
END
GO
