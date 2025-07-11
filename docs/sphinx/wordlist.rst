.. _wordlist:

Ordliste
========

Ordlisten definerer fortolkningen af de begreber, der anvendes i dokumentation og udgør med historik det kontraktlige dokument "Data Dictionary".


Admin-rolle
-----------

Beskriver en systemrolle der ved tildeling giver en bruger adgang til at tildele læse-adgang (read-rolle) til et givent element.


Administrationssystem
---------------------

Når administrationssystem anvendes i sammenhæng med datafordeleren og adresseopslagsregistret vil det oftest refererer til de administrative brugergrænseflader der stilles til rådighed for administrationsbrugere. 


Administrator
-------------

En bruger der har adgang til administrationssystemet og kan bruge dette til at give andre brugere adgang til datafordeleren og dens administrationssystem.

En administrator har alle rettigheder i administrationssystemet.


Anvender
--------

En bruger eller et system der gør brug, eller ønsker at gøre brug, af datafordelerens services. Se også [[Ordliste|dataanvender]]


API: Application Programing Interface
-------------------------------------

Snitfladen mellem forskellige systemer anvender et API, ofte omtalt som interface, til at udveksle oplysninger med hinanden.


Autentification
---------------

En anvender autentificeres i forhold til identitet og tilknytninger, så der er sikkerhed for hvem som logger på systemet.


Autorisation
------------

Når en anvender er autentificeret, kan vedkommende autoriseres til roller, rettigheder og profiler.


Bruger
------

Begrebet bruger vil i datafordeler-sammenhæng typisk referere til en anvender, der logger ind via datafordelerens egen identity provider og som har sine brugerprofiler med rettigheder defineret i datafordelerens database. Bruger vil typisk blive til dataanvender, når der hentes data og er underlagt datafordelerens [[Ordliste|betingelser]] - Se også [[Ordliste|Bruger/superbruger i adresseopslagsregistret]].


Brugerprofil i datafordeleren
-----------------------------

En brugerprofil definerer en samling af systemroller og en eventuel liste af områdeafgrænsninger. Brugerprofiler tildeles til anvendere for at give dem adgang til datafordelerens services.


Bruger/superbruger i adresseopslagsregistret
--------------------------------------------

Bruger og Superbruger er to begreber i adresseopslagsregisterets administrationsmodul. En bruger kaldes også en kommunal bruger, da vedkommende kan oprette, redigere og nedlægge veje, B-numre og adresser i sin egen kommune. En superbruger kan oprette, redigere og nedlægge brugere, procestilstande, kommunale data samt fælles oplysninger om kommuner, distrikter, postnumre, lokaliteter.


.. _certificat:

Certificat
----------

Certifikat bruges til autentificere at vedkommende bruger eller virksomhed virkelig også er anvender, som de siger at de er. Det svarer til en avanceret elektronisk underskrift, hvor andre har garanteret for underskriftens ægthed.


.. _certificat-typer:

Certificat-typer OCES - MOCES - VOCES - FOCES - POCES
-----------------------------------------------------

Offentlige Certifikater til Elektroniske Services (OCES) er en dansk type, der findes i versioner til medarbejdere (MOCES), virksomheder (VOCES), funktioner (FOCES) og personer (POCES). Sammen med certifikat-filen følger der oftest en eller flere nøglefiler, der er en del af sikkerheden. Certificaterne anvendes i løsninger som f.eks. NemID. I datafordeleren kan anvendes MOCES, VOCES og FOCES.


Claim
-----

Et claim er en attribut i en SAML token der beskriver en egenskab ved det som tokenet identificerer. I datafordeler-sammenhæng anvendes claims til at angive hvilke brugerprofiler en anvender har tilknyttet.


Data i datafordeleren
---------------------

De tilgængelige data i datafordeleren er data fra forskellige myndighedsregistre og andre registrer. Hvad de enkelte data omfatter, og regler for deres anvendelse, findes beskrevet i deklarationen af de enkelte datakilder. Her findes også den supplerende viden om de bagvedlliggende registre, der skal findes hos de ansvarlige udgivere. Datafordelerens opgave er at samle eksisterende og nye data for at udgive dem i et grunddata-format, der for største delen svarer til den `danske grunddatamodel. <http://data.gov.dk/>`_


Dataansvarlig
-------------

Den offentlige myndighed, der alene eller sammen med andre afgør, til hvilket formål og med hvilke hjælpemidler, der må foretages behandlinger af oplysninger.


Dataanvender
------------

En bruger, der benytter sig af data hentet fra Datafordeleren og som er underlagt Datafordelerens [[Betingelser]]. Det omfatter enhver operation eller række af operationer med eller uden brug af elektronisk databehandling, som oplysninger gøres til genstand for. 


Databehandler
-------------

Den fysiske eller juridiske person, offentlige myndighed, institution eller ethvert andet organ, der behandler oplysninger på den [[Ordliste|Dataansvarlig]]es vegne.


Datafordelermotor
-----------------

Det grundlæggende stykke software i datafordeleren. Motoren binder de enkelte registerplugins sammen og eksponerer interfaces til dataanvenderne, så de kan foretage forespørgsler i registrene.


Datafordelertoken
-----------------

En datafordelertoken er en token udstedt af datafordelerens STS der indeholder identiteten på en anvender samt anvenderens tilknyttede brugerprofiler.


Datakomponenter
---------------

Bestanddelene i et register omtales ved forskellige ord. I Datafordeleren anvendes begreber, der svarer til den danske grunddatamodel.

======== ======================================================================================= ===================================================
Begreb   Beskrivelse                                                                             Andre tilsvarende ord
======== ======================================================================================= ===================================================
Service  Udstiling af Registerdata, f.eks. CPR                                                   Database, Datasystemet
Entitet  Forvaltningsobjekter, der udstilles via webservices, f.eks. personoplysninger           Tabel *eller* tabelrækker
Attribut Navngiven dataværdi på en entitet, f.eks. efternavn (attribut) > Hansen (attributværdi) Kolonner med felter, der har feltværdier
======== ======================================================================================= ===================================================


Dato og klokkeslæt
------------------

Internt i datafordelerens stemples alle oplysninger med dato og klokkeslæt i forhold til det universelle koordinerede tidspunkt UTC, der danner grundlag for at beregne tidszoner og sommertid.


Forespørgsels-API (FAPI)
------------------------

Datafordelerens forespørgsels-API udgør det interface, som dataanvenderne tilgår. Det er et versioneret interface, der genereres automatisk, når en tjeneste konfigureres i et registerplugin.


Generel API (GAPI)
------------------

Det generelle API, der udstilles af datafordeleren til registrene, så de kan notificere datafordeleren – og dermed anvenderne - om ændringer.


Ekstern identity provider
-------------------------

En ekstern identity profider er en SAML identity provider i en organisation uden for datafordeleren, som er blevet registreret i datafordeleren, og dermed har fået ret til at tildele rettigheder i datafordeleren til sine brugere via brugerprofiler/claims i udstedet tokens.

Før en token fra en ekstern identity provider kan anvendes til at give adgang til datafordeleren skal den via datafordelerens STS veksles til en datafordelertoken. Hvilke brugerprofiler en ekstern identity provider kan tildele sine brugere administreres via datafordelerens administrationssystem.


Identity Provider (IdP)
-----------------------

En SAML identity provider er en service der kan autentikere en organisations brugere og udstede SAML tokens, der verificerer brugerens identitet. Services, der stoler på en given identity provider kan så bruge disse tokens til at verificere hvem en bruger fra organisationen er.


Områdeafgrænsning
-----------------

En områdeafgrænsning betegner et område der bruges til at afgrænse adgangen til data. Det kunne for eksempel være en kommune, der bruges til at adgangen til persondata til kun borgere fra denne kommune.


Open Source
-----------

Programkoden til Datafordeler og Adresseopslagsregistret er underlagt bestemmelserne om Open source kode under licensbestemmelserne "Mozilla Public License Version 2.0":https://www.mozilla.org/en-US/MPL/2.0/.


.. _parse:

Parse
-----

Gennemgang og opløsning af en tekst grammatisk udføres af systemet for at genkende indhold, udtrække særlige data og skrive data i andre passende formater til systemets videre formål.


Plugin
------

En plugin er et stykke kode, der udvider et andet stykke kode. I datafordeleren bruges plugins til at kommunikere med de enkelte registre. Hvert plugin kobler den registerspecifikke API sammen med synkroniserings-API'et. Derudover definerer det enkelte plugin også datamodellen for det enkelte register, og hvordan data repræsenteres i datafordeleren.


Read-rolle
----------

En systemrolle der bruges til at give anvendere adgang til at tilgå data.


Registerspecifik API
--------------------

Hvert register har sit eget specifikke API, der giver adgang til data i registeret. Datafordeleren definerer ikke den konkrete udførsel af disse API'er, men stiller visse funktionelle krav til dem. Her udgør det registerspecifikke API den snitflade, som det enkelte plugin tilgår på sit register. Implementering af kommunikation med registerspecifikke API'er varierer fra plugin til plugin.


Security Assertion Markup Language (SAML)
-----------------------------------------

Datafordeleren gør brug af SAML som defineret i OIO- standarderne (Offentlig Information Online), se “OIO Identity-based Web Services” på http://digitaliser.dk/resource/526486


.. _security-token-service:

Security Token Service (STS)
----------------------------

STS’ens opgave er at udstede SAML2 tokens, der identificerer en bruger og hvilke adgange brugeren har til den ønskede service


Serviceudbyder
--------------

En bruger der har adgang til administrationssystem, og via dette har ret til at tildele andre anvendere adgang til at tilgå data. Hvilke data en serviceudbyder kan give adgang til, defineres af hvilke admin-roller serviceudbyderen er blevet tildelt.


Sprogkode
---------

Sprogkoden indikerer det anvendte sprog på hjemmesider, i brugerflader og mange andre steder. Ud fra sprogkoder kan systemer vælge de rigtige sorteringer, ordforslag og andet. I datafordeler-sammenhænge anvendes de almindeligt anvendte sprogkoder og internationale navne fra `ISO 639‑1. <https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes>`_

* Sprogkode grønlandsk [kl, kalaallisut)
* Sprogkode dansk [da, dansk] 
* Sprogkode engelsk [en, English]. 

Der skelnes ikke mellem lokale eller nationale dialekter. Sprogkoder forveksles ofte med nationale koder (som ikke siger noget om sproget), f.eks. kendemærker på biler: KN/GL, DK, GB/UK/US/CDN/AUS/… Nationskoder anvendes ikke i datafordeleren.


Superbruger i Adresseopslagsregistret
-------------------------------------

Se Bruger/superbruger i adresseopslagsregistret.


Synkroniserings-API (SyncAPI/SAPI)
----------------------------------

SyncAPI er et internt Java-interface i datafordeleren, der implementeres i hver enkelt registerplugin. Det sikrer, at datafordelerens motor kan bruge et fælles interface til at foretage synkronisering og håndtering af beskedfordeling for alle registrene.


System-til-system bruger
------------------------

En anvender der identificeres i systemet via et klientcertifikat. System-til-system brugere tildeles rettigheder i datafordeleren ved tilknytning af brugerprofiler til deres konto via administrationssystemet for datafordeleren.


Systemadgangsbruger
-------------------

Se system-til-system bruger.


Systemrolle
-----------

En systemrolle er en rolle defineret af datafordeleren der bruges ved kontrol af adgange til forskellige dele af datafordeleren. Systemroller skal samles i brugerprofiler sammen med eventuelle områdeafgrænsninger før de kan tildeles til anvendere af systemet.


Systemnavne
-----------

Et systemnavn anvendes i den verbale, daglige kommunikation og i oversigter af forskellige typer. Navnene findes i flere udgaver som f.eks. fuldt eller officielt navn (det fulde egennavn), kaldenavn (en kortere og mere mundret navn for et system), forkortelser (til oversigter, tekniske beskrivelser med mere) og endelig et webnavn, der ofte også anvendes som brand.  Hvis der ikke er angivet sprogkode, anvendes det samme navn på alle sprog.


System: Datafordeleren
^^^^^^^^^^^^^^^^^^^^^^

Kernesystemet for sikker udgivelse af data fanget i  myndighedsregistre og stillet til rådighed for dataanvendere.

* Fuldt navn: "Qassutit":https://oqaasileriffik.gl/langtech/martha/?st=qassutit#result [kl, grønlandsk navn forslag er ikke afgjort] – Den Grønlandske Datafordeler [da] – The Greenlandic Data Distributor [en]
* Kort navn: Datafordeleren
* Forkortelse: DAFO
* URL: https://data.gl - i test: https://test.data.gl


System: Najugaq – Adresseopslagsregister
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

* Fuldt navn: 
* Kort navn: 
* Forkortelse:
* URL: 


System: DAFO-admin - Datafordelerens administrationsprogram
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

* Fuldt navn: 
* Kort navn: 
* Forkortelse:
* URL: 


System: DAFO Ops
^^^^^^^^^^^^^^^^

Serviceplatformen hos leverandørerne til understøttelse af deres datafordeler-drift (en: operations).


System: DAFO-dok - Dokumentation til datafordeler og adresseopslagsregister
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Online-dokumentationen i drifts- og udviklingsleverandørernes service-platform. Dokumentation er offentlig tilgængeligt, undtagen for de dele som indeholder fortrolige oplysninger.


System: DAFO-kode - Programkoden til datafordeler og adresseopslagsregister
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

* Fuldt navn: 
* Kort navn:
* Forkortelse:
* Note: Koden til systemet er [[Ordliste|open source]] og tilgængelig for alle på internettet. De fortrolige oplysninger om konfigurationer og andet udveksles gennem lukkede systemer og er ikke medtaget.


System: The Greenlandic Gazettteer***
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

* Fuldt navn: Stednavneregistreret [da]
* Kort navn:
* Forkortelse:
* Note: Registreret er en vigtig kilde til stednavne for Datafordeleren og de tilknyttede registre.


Tegnsæt
-------

Et tegnsæt er oversættelsen mellem computerens koder og de bogstaver som mennesker anvender. Datafordeleren henter data fra kilder med forskellige tegnsæt og omdanner dem til tegnsæt UTF-8.
