.. _plugins:

Dataimport
==========

Dataimport for CPR og CVR kører med jævne mellemrum, hvor der hentes data fra de respektive kilder,
de modtagne data parses og tilføjes databasen, idet data lægges sammen med den eksisterende struktur i databasen.
Både tidspunktet for import, kildens adresse, evt. brugernavn og password, samt supplerende data, indstilles i DAFO admin.

Der er kun nødvendigt for én server pr. database at køre import, da databasen (med de importerede data) deles mellem flere
applikations-instanser bag en loadbalancer, og disse har derfor adgang til de samme data.
Hvilken server der kører imports kan indstilles i den server-specifikke konfigurationsfil (oftest core/local_settings.properties),
med variablen dafo.pull.enabled
