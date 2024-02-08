IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'Datafordeler')
BEGIN
  CREATE DATABASE Datafordeler;
END;

IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'DatafordelerConfig')
BEGIN
  CREATE DATABASE DatafordelerConfig;
END;

GO
