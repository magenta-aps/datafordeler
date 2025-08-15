.. _api-connection:

API forbindelse
===============

Datafordeleren udstiller data til andre systemer via https://test.data.gl, hvorfra anvender-systemer bruger disse data. Denne vejledning viser hvordan et system taler med datafordeleren. Det forudsættes at læseren er bekendt med skrivning og afsendelse af HTML samt læsning i JSON.

Adgang til CPR- og CVR-data kræver autorisation og autenficering med :ref:`STS Secure Token Service, <sts-secure-token-service>` og det er kun muligt for myndigheder efter forudgående aftale med Den Grønlandske Digitaliseringsstyrelse.


Anvendelse uden autorisering og autentificering
-----------------------------------------------

Som eksempel vil et anvendersystem gerne have en liste med alle lokaliteter i Grønland. Det er ikke personfølsomme data og vil være tilgængelig for alle systemer på internettet.

Hvilke kommandoer, der kan anvendes på et API, findes på :ref:`API-deklarationer, <api-declarations>` hvor det ses hvad man skriver for at finde bestemte data.


Her er trinene i at få offentlige data hjem uden autorisation og autentificering:
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

1. Systemet sender en besked med de søgte oplysninger - for eksempel ``https://data.gl/geo/locality/1/rest/search?lokalitetskode=*&pageSize=100000``
2. Resultatet kommer tilbage i et :ref:`JSON-format. <json-results>` Herfra kan anvender-systemet læse og bruge de ønskede oplysninger i henhold til :ref:`betingelser <agreements>` for både datafordeler og det aktuelle register.
3. Bemærk at eksemplet sætter resultatafgrænsningen til 100000. Søgningen er pagineret, og standardstørrelsen er 10 resultater.


Anvendelse med autorisering og autentificering
----------------------------------------------

Et eksempel er et anvendersystem, der gerne vil kende navn og adresse for et CPR-nummer. CPR-numre er personfølsomme data, så anvender-systemet skal dokumentere sin ret og lovhjemmel til disse data, oplyse de korrekte personlige oplysninger for brugeren, der efterspørger data. Datafordeleren logger alle disse oplysninger af hensyn til sikkerhed og lovgivning. Hvordan den sikre tilgang virker, er beskrevet i :ref:`STS og hvordan datafordeleren anvender det. <sts-and-how-it-is-used>`

I dette eksempel kaldes personnummeret ``ddmmåååånnnn``, og det er i virkeligheden en række af tallene fra 0 til 9. 


Forhåndsbetingelserne for adgang til fortrolige data er:
--------------------------------------------------------

* Aftale med den grønlandske digitaliseringsstyrelse og aftale med de aktuelle dataansvarlige myndigheder for de data, som skal hentes, og hvilke måde som systemet vil give sig til kende overfor Datafordeleren.

* Anvendersystemet skal have installeret og anvende et FOCES-certifikat, der udstedes til offentlige myndigheder og andre. Alternativt skal anvendersystemet have en aftale med en Identity-provider (IpS), som kan være en tredjepart, det kan være en kommunes eget system eller det kan være en aftale om at anvende Datafordelerens identity provider. Hvorledes token fungerer er beskrevet i :ref:`STS og hvordan datafordeleren anvender det. <sts-and-how-it-is-used>`


Her er trinene i at hente data med autorisering og autentificering
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Forhåndsbetingelser fra forrige afsnit skal være opfyldt:

1. Systemet klargør og sender nu en besked med følgende komponenter:

    * Certifikatet  indsættes i header som attribut.
    * De søgte oplysninger skrives i URL'en: ``https://data.gl/cpr/person/1/rest/search?personnummer=*``

2. Datafordeleren veksler certifikat til en token, hvis ikke der medfølger en gyldig token, og iværksætter søgningen tilpasset rettighederne i token.

3. Resultatet kommer tilbage til anvendelse i :ref:`JSON-format. <json-results>` Herfra kan anvender-systemet læse og bruge de ønskede oplysninger i henhold til :ref:`betingelser <agreements>` for både datafordeler og det aktuelle register.

Du kan se mere om API-deklarationerne på siden om :ref:`API. <dafo-api>`
