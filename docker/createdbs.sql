IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'Datafordeler')
BEGIN
  CREATE DATABASE Datafordeler;
END;
GO
