STS og adgangsrettigheder
=========================

Dette afsnit beskriver hvorledes rettigheder defineres og lagres i datafordeleren, så de kan bruges i forbindelse med udstedelse af tokens. 

Afsnittet beskriver ikke de fuldstændige procedurer for hvordan rettigheder ændres via datafordelerens brugergrænseflade, da denne information vil være at finde i dokumentationen for brugergrænsefladen.


Systemroller
------------

Systemroller defineres af datafordelerens plugins, der skaber forbindelsen til de individuelle registre. De bruges til at alle steder hvor det er nødvendigt at have specielle rettigheder for at gøre brug af datafordelerens services.

Systemroller defineres på tre niveauer:

• Serviceniveau - Bruges til at give adgang til en hel service, f.eks. CVR-data

• Entitetsniveau – Bruges til at give adgang til enkelte dataentiteter inden for et givent register, f.eks. ejerregistreret i CVR.

• Attributniveau – Bruges til at give adgang til enkelte attributter på entiteter, f.eks. objekter tilknyttet et bestemt CPR-nummer.

Derudover er det muligt at angive tilpassede systemroller, der kan bruges til at implementere rettigheder der ikke kan dækkes af ovenstående. Disse roller gives betegnelse ”Custom” i systemet. Custom-roller bruges for eksempel til at definere om en bruger i systemet er administrator eller serviceudbyder.

For hver rolletype defineres der både read-roller og admin-roller. Read-roller bruges til at give adgang til data og services. Admin-roller bruges til at give administratorer og serviceudbydere mulighed for at tildele read-roller til anvendere.

I implementeringen af hver enkelt service angives hvilke systemroller den har brug for i forbindelse med kontrol af rettigheder og logikken for hvordan systemrollerne bruges til at verificere adgangen.

Systemrollerne fødes således under implementering af plugins og services i datafordeleren og stilles derfra til rådighed for datafordelerens administrationssystem, hvor datafordelerens administratorer og serviceudbydere kan tildele dem til datafordelerens anvendere.

For overskuelighedens skyld oprettes der kun systemroller for services, hvor der er behov for rettighedskontrol. For eksempel vil services uden følsomme data ikke have systemroller, da det ikke vil være nødvendigt at kontrollere adgangen til disse services. Ligeledes vil der ikke eksistere attribut-systemroller for en adgangsbeskyttet entitet, hvis denne entitet kun kan tilgås i sin helhed.Så vil der i det tilfælde ikke være behov for kontrol på individuelle attributter.

Illustrationen herunder viser model for systemroller i datafordeleren og hvordan systemroller defineres under udvikling af et plugin til datafordeleren.

!Systemroller_plugin.png!


Områdeafgrænsning
-----------------

Områdeafgrænsning giver en service i datafordeleren mulighed for at definere en række områder som adgangen til en service kan opdeles i. Det er op til implementeringen af servicen at definere hvad områderne er, hvad de hedder, hvad de dækker og hvordan de teknisk bruges til at afgrænse forespørgsler til servicen.

Områdeafgrænsninger kan tilknyttes til brugerprofiler, når disse oprettes i datafordelerens administrationssystem. De vil begrænse anvendere der tildeles brugerprofilen til data fra de valgte områder.


Brugerprofiler
--------------

Rettigheder i datafordeleren tildeles til anvendere via brugerprofiler. En brugerprofil dækker en række systemroller der giver adgang til deres respektive service/entitet/attribut og en eventuel liste af områdeafgrænsninger.

Brugerprofiler kan oprettes af administratorer og serviceudbydere via datafordelerens administrationssystem.


Tildeling af rettigheder
------------------------

Rettigheder til anvendelse af datafordeleren tildeles af administratorer og serviceudbydere til anvendere via datafordelerens administrationssystem.

Tildeling af rettigheder til en given anvender, der logger ind via datafordelerens identity provider eller via system-til-system certifikat vil typisk være en todelt process: Først oprettes en række brugerprofiler, der beskriver de services, data og områder, anvenderen skal have adgang til. Efterfølgende tildeles disse brugerprofiler til anvenderens brugeridentitet. Skal flere anvendere have de samme adgange, vil det være muligt at genbruge tidligere oprettede brugerprofiler, og det første trin kan springes over.

Tildeling af rettigheder til brugere der kommer fra en ekstern identity provider, følger en anden model. Her skal den eksterne identity provider først oprettes i datafordelerens administrationssystem. Derefter oprettes en række brugerprofiler, der definerer rettigheder som den eksterne provider skal kunne uddele og disse tilknyttes den oprettede provider. Denne liste af brugerprofiler vil fungere som en ”white-list” der bruges til at godkende brugerprofiler angivet i en SAML token fra den eksterne provider, så de kan blive videreført til en datafordelertoken i forbindelse med login til datafordeleren.

Det skal bemærkes at samme metode bruges til at tildele read- og admin-roller i administrationssystemet. Retten til at oprette brugerprofiler, der indeholder admin-roller, vil blot være afgrænset til brugere med den specielle rolle, der gør dem til administratorer.

Illustration herunder viser datamodel for bruger/profil/rolle system igennem administrationssystemet og oversættelse af brugerprofiler til systemroller og områdeafgrænsninger.

!Datamodel_bruger_profil_system.png!
