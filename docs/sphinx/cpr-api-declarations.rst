.. _cpr-api-declarations:

CPR API-deklaration
===================

CPR API udstiller persondata tilknyttet Grønland fra `Det Civile Personregister. <https://cpr.dk>`_


Datakilder
----------

Data kommer fra CPR-kontoret en gang i døgnet i midnatstimerne og opdateres i Datafordeleren. 

Data i CPR-registret vedligeholdes af kommuner i andre systemer. Desuden opdaterer Indenrigsministreriet og Styrelsen for Dataforsyning og Effektivisering fælles oplysninger. 


Kontakt API
-----------

* API-Udstilling og selvdeklaration:  https://data.gl/cpr/person/1/rest/
* Udvidet dokumentation til API'er: https://redmine.magenta-aps.dk/projects/dafodoc/wiki/API
* Betingelser for anvendelse: https://redmine.magenta-aps.dk/projects/dafodoc/wiki/betingelser


Entiteter og felter
-------------------

* Domænemodellen Person fra den danske `grunddatamodel <http://data.gov.dk>`_ dækker omfanget.


Eksempler på kald
-----------------

Disse eksempler på kald vil næsten alle give en fejl, da der kræves myndighedsadgang med STS.

* https://data.gl/cpr/person/1/rest/
* https://data.gl/cpr/person/1/rest/search?personnummer=2902800752
* https://data.gl/cpr/person/1/rest/search?fornavn=Tester
* https://data.gl/cpr/person/1/rest/search?efternavn=Testersen&fornavn=Tester
* https://data.gl/cpr/person/1/rest/04e1b9f9-cf82-4cbb-bb89-305b528ae34f

NB! Ovenstående persondata er eksempler på hvordan kald kunne se ud, hvis de aktuelle data fandtes og man var blevet autoriseret til at udføre kaldene.


Fejlkoder
---------

Succes-svar og fejlkoder er fælles for alle API'er i datafordeleren og oversigten findes på :ref:`API resultater. <api-results>`
