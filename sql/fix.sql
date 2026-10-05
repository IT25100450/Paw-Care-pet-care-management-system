USE PawCare;
GO

ALTER TABLE Vet_Appointment DROP CONSTRAINT fk_vetapp_coordinator;
GO

ALTER TABLE Vet_Appointment DROP COLUMN Staff_ID;
GO
