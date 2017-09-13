CPR API-deklaration
===================

CPR API udstiller persondata tilknyttet Grønland fra "Det Civile Personregister":https://cpr.dk. 

Datakilder
----------

Data kommer fra CPR-kontoret en gang i døgnet i midnatstimerne og opdateres i Datafordeleren. 

Data i CPR-registret vedligeholdes af kommuner i andre systemer. Desuden opdaterer  Indenrigsministreriet og Styrelsen for Dataeffektivisering og Effektivisering fælles oplysninger. 

Adgang kræver autorisation og autenficering med [[STS Secure Token Service]], og det er kun muligt for myndigheder efter forudgående aftale med Den Grønlandske Digitaliseringsstyrelse og CPR-kontoret. 

Datamodeller
------------

* Datamodel CPR-data modtages i klassisk CPR-format og overføres til domænemodellen Person fra den danske grunddatamodel":http://data.gov.dk, der også er model for CPR-data i Datafordeleren.

* Tegnsæt i CPR-data er ISO-8859-1 (Latin 1), der er en komplet delmængde af Datafordelerens tegnsæt UTF-8, så der foretages ingen tegnkonvertering.

* Klokkeslæt i CPR-data har klokkeslæt stemplet uden angivelse af tidszone eller relation til UTC. Konvertering til grunddatamodel og den grønlandske tilpasning kræver kendskab til UTC eller tidszone. Tidsstempling er i testperioden under aftestning og grundlaget kan skifte. 

Kontakt API
-----------

* API-Udstilling og selvdeklaration:  https://test.data.gl/cpr/person/1/rest/
* Udvidet dokumentation til API'er: https://redmine.magenta-aps.dk/projects/dafodoc/wiki/API
* Betingelser for anvendelse: https://redmine.magenta-aps.dk/projects/dafodoc/wiki/betingelser

Entiteter og felter
-------------------

* Domænemodellen Person fra den danske grunddatamodel:http://data.gov.dk dækker omfanget.

Eksempler på kald
-----------------

Disse eksempler på kald vil næsten alle give en fejl, da der kræves myndighedsadgang med STS.

** https://test.data.gl/cpr/person/1/rest/
** https://test.data.gl/cpr/person/1/rest/search?cpr=2902800752
** https://test.data.gl/cpr/person/1/rest/search?firstName=zakæus
** https://test.data.gl/cpr/person/1/rest/search?lastName=Eqqarleriit&firstName=Benjamin
** https://test.data.gl/cpr/person/1/rest/04e1b9f9-cf82-4cbb-bb89-305b528ae34f

NB! Ovenstående persondata er eksempler på hvordan kald kunne se, hvis de aktuelle data fandtes og man var blevet autoriseret til at udføre kaldene.

Fejlkoder
---------

Succes-svar og fejlkoder er fælles for alle API'er i datafordeleren og oversigten findes på [[API resultater]].  

{{include(Undertekst)}}
