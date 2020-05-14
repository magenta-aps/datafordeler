CPR
============

This project fetches information from CPR.
It reads two type of information and persists it in the database of datafordeleren:

##Persons
When this project was initially started we got somthing called "Etableringsudtræk" this is the baseline of personinformation.
Every day changes to the originally "etableringsudtræk" is fetched from the FTP-server that we got from CPR-kontoret.
All files is stored on the filesystem on the server, and the files is not keept by CPR-kontoret for more than two month

If it is nessesary to clean the database and reload information pull needs to be called with {"plugin":"cpr","remote":false}.
This is to mak sure that the previous fethceh files from CPR is parsed before the daily ammendments is added.

##Roads
The Danish roadregiser is imported manually by copying the file from CPR-kontoret, and place it on the server.
The roadregister contains information about how a roadcode and munipialicitycode can be translated into a readable roadname.
This register is used for generating readable Danish adresses in datafordeleren.


The Danish roadregister can be downloaded manually from the following homepage:
https://cpr.dk/kunder/gratis-download/vejregister-og-udtraeksbeskrivelse/



