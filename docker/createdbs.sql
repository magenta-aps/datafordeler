IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'Datafordeler')
BEGIN
  CREATE DATABASE Datafordeler;
END;
ALTER DATABASE Datafordeler SET COMPATIBILITY_LEVEL = 130


IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'DatafordelerConfig')
BEGIN
  CREATE DATABASE DatafordelerConfig;
END;
ALTER DATABASE DatafordelerConfig SET COMPATIBILITY_LEVEL = 130

GO
