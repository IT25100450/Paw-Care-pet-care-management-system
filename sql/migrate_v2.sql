-- ============================================================
-- PawCare Migration Script v2
-- Run these against your existing PawCare database in SSMS
-- ============================================================

-- 1. Add Status column to Booking table (Item 4)
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'Booking' AND COLUMN_NAME = 'Status'
)
BEGIN
    ALTER TABLE Booking ADD Status VARCHAR(20) NOT NULL DEFAULT 'pending';
    PRINT 'Added Status column to Booking';
END

-- 2. Make Vet_Appointment.Staff_ID nullable (Item 2)
IF EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'Vet_Appointment' AND COLUMN_NAME = 'Staff_ID' AND IS_NULLABLE = 'NO'
)
BEGIN
    DECLARE @fk NVARCHAR(200);
    SELECT @fk = tc.CONSTRAINT_NAME
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc
    JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE kcu ON tc.CONSTRAINT_NAME = kcu.CONSTRAINT_NAME
    WHERE tc.TABLE_NAME = 'Vet_Appointment'
      AND kcu.COLUMN_NAME = 'Staff_ID'
      AND tc.CONSTRAINT_TYPE = 'FOREIGN KEY';
    IF @fk IS NOT NULL
        EXEC('ALTER TABLE Vet_Appointment DROP CONSTRAINT ' + @fk);
    ALTER TABLE Vet_Appointment ALTER COLUMN Staff_ID INT NULL;
    ALTER TABLE Vet_Appointment
        ADD CONSTRAINT fk_vetapp_coordinator
        FOREIGN KEY (Staff_ID) REFERENCES Veterinary_Coordinator (Staff_ID);
    PRINT 'Made Vet_Appointment.Staff_ID nullable';
END

-- 3. Add Status to Vet_Appointment
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'Vet_Appointment' AND COLUMN_NAME = 'Status'
)
BEGIN
    ALTER TABLE Vet_Appointment ADD Status VARCHAR(20) NOT NULL DEFAULT 'pending';
    PRINT 'Added Status to Vet_Appointment';
END

-- 4. Create Customer_Order table
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'Customer_Order')
BEGIN
    CREATE TABLE Customer_Order (
        Order_ID   INT IDENTITY PRIMARY KEY,
        Owner_ID   INT NOT NULL,
        Order_Date DATETIME NOT NULL DEFAULT GETDATE(),
        Status     VARCHAR(20) NOT NULL DEFAULT 'confirmed',
        CONSTRAINT fk_order_owner FOREIGN KEY (Owner_ID) REFERENCES Pet_Owner (Owner_ID)
    );
    PRINT 'Created Customer_Order table';
END

-- 5. Create Order_Item table
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'Order_Item')
BEGIN
    CREATE TABLE Order_Item (
        Order_Item_ID INT IDENTITY PRIMARY KEY,
        Order_ID      INT NOT NULL,
        Product_ID    INT NOT NULL,
        Quantity      INT NOT NULL DEFAULT 1,
        Unit_Price    DECIMAL(10,2) NOT NULL,
        CONSTRAINT fk_orderitem_order   FOREIGN KEY (Order_ID)   REFERENCES Customer_Order (Order_ID),
        CONSTRAINT fk_orderitem_product FOREIGN KEY (Product_ID) REFERENCES Product (Product_ID)
    );
    PRINT 'Created Order_Item table';
END

PRINT 'Migration v2 complete.';
