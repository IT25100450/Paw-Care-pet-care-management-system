-- =================================================================================
-- PawCare Migration v3
-- Purpose: Add Inquiry table for Customer Care inquiries
-- =================================================================================

USE PawCare;
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Inquiry')
BEGIN
    CREATE TABLE Inquiry (
        Inquiry_ID INT IDENTITY PRIMARY KEY,
        Owner_ID INT,
        Subject VARCHAR(150),
        Message VARCHAR(1000),
        Status VARCHAR(20) DEFAULT 'Pending',
        Created_At DATETIME DEFAULT GETDATE(),
        Response VARCHAR(1000),
        Responded_By INT,
        CONSTRAINT fk_inquiry_owner FOREIGN KEY (Owner_ID) REFERENCES Pet_Owner(Owner_ID),
        CONSTRAINT fk_inquiry_staff FOREIGN KEY (Responded_By) REFERENCES Staff(Staff_ID)
    );
    PRINT 'Created Inquiry table';
END
ELSE
BEGIN
    PRINT 'Inquiry table already exists';
END
GO
