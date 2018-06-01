.. _api-results:

API resultater
==============

REST-API'er returnerer `HTTP svarkoder. <https://tools.ietf.org/html/rfc7231>`_ I det følgende findes de hyppigst forventede svarkoder til en række almindelige forespørgsler.


Forventede resultater
---------------------

I dette afsnit er eksempler på API-forespørgsler, hvor der er succes med at få besked om det søgte findes eller ej. Dele af teksten er anonymiseret med ˽ som erstatningstegn.


200 - OK - En succesfuld forespørgsel med svar
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

URL: https://data.gl/cpr/1/rest/search?cpr=˽˽˽˽˽˽˽˽˽˽

Kendetegnet for en velykket søgning med resultat er indhold i feltet ``"results": [ _indhold_ ]``. Datafordeleren sætter ikke statuskode på resultatet.::

    {
        "path": "/cpr/person/1/rest/search",
        "terms": "https://doc.data.gl/terms",
        "requestTimestamp": "2017-07-07T05:01:10.141-02:00",
        "responseTimestamp": "2017-07-07T05:01:12.266-02:00",
        "username": "[c˽˽˽r@m˽˽˽k]@[˽˽˽]",
        "page": 1,
        "pageSize": 10,
        "results": [
            {
    ...
    _indhold_
    ...
                "personnummer": "˽˽˽˽˽˽˽˽˽˽",
                "domain": "cpr",
                "uuid": "c67aee2b-98fb-4829-b2d1-38de331b167b"
            }
        ]
    }

204 - No content - En succesfuld forespørgsel med et tomt resultat
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

URL: https://data.gl/cpr/person/1/rest/search?lastName=Jansson

Kendetegnet for en vellykket søgning uden resultat er linjen ``"results": []``. Forventes der et indhold, skal man efterse parametrene og det søgte mønster. Datafordeleren sætter ikke statuskode på resultatet.::

    {
        "path": "/cpr/person/1/rest/search",
        "terms": "https://doc.data.gl/terms",
        "requestTimestamp": "2017-07-07T05:02:10.056-02:00",
        "responseTimestamp": "2017-07-07T05:02:10.087-02:00",
        "username": "[c̺˽˽˽r@m˽˽˽k]@[˽˽˽]",
        "page": 1,
        "pageSize": 10,
        "results": []
    }

Andre resultater end de forventede
----------------------------------

Her findes de typiske fejl, som brugere kan møde i datafordelerens API. REST API'er anvender fejlmeddelelserne i `HTTP-protokollen, <https://tools.ietf.org/html/rfc7231#section-6.1>`_ der rummer den fulde oversigt over mulige fejlkoder.

400 - Bad request - Fejl i parametre
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Forespørgslen indeholder parametre eller mønstre som API ikke kan genkende. Efterse parametre, deres stavning og om navnene findes på API'et. Navne er versalfølsomme og skal skrives præcist. Mønstre er ikke versalfølsomme.::

    {
        "timestamp": 1499421050629,
        "status": 400,
        "error": "Bad Request",
        "exception": "dk.magenta.datafordeler.core.exception.InvalidClientInputException",
        "message": "Invalid UUID string: find=Finn",
        "path": "/cpr/person/1/rest/find=Finn"
    }

URL'en https://data.gl/cpr/person/1/rest/find=Finn giver fejl, da 'find' ikke er et kendt navn på API'et. Faktisk svarer https://data.gl/cpr/person/1/rest/find til et opslag efter UUID, hvor dette UUID er "Finn", hvilket ikke er gyldigt.


403 - Forbidden - Ingen adgang til API
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Forespørgslen mangler en token som beskrevet i :ref:`STS Secure Token Service. <sts-secure-token-service>` I beskeden kan desuden være anført brugernavn for login, så bruger kan efterse om det er det korrekte navn.::

    {
        "timestamp": 1499420553669,
        "status": 403,
        "error": "Forbidden",
        "exception": "dk.magenta.datafordeler.core.exception.AccessDeniedException",
        "message": "User [anonymous]@[anonymous] does not have access to ReadCpr",
        "path": "/cpr/person/1/rest/search"
    }


401 - Unauthorized - Token er ubrugelig
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Token er ikke gyldig. Anvender-systemet skal efterse at aftale er på plads om sikker adgang til Datafordeleren, at certifikater er på plads og i orden, at brugeren er tildelt de rette rettigheder i anvendersystemet og hvorvidt anvender er oprettet med de ønskede rettigheder i Datafordeleren af den registeransvarlige eller den dataansvarlige for det pågældende register.::

    {
        "timestamp": 1499421684724,
        "status": 401,
        "error": "Unauthorized",
        "exception": "dk.magenta.datafordeler.core.exception.InvalidTokenException",
        "message": "Could not parse authorization token",
        "path": "/cpr/person/1/rest/search"
    }


401 - Unauthorized - Token is older than 43200 seconds
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Tiden er udløbet, så forbindelsen skal fornys. I de fleste anvender-systemer skal bruger blot gentage sin forespørgsel og så sørger anvender-systemet for at forny brugers token.::

    {
        "timestamp": 1499420061909,
        "status": 401,
        "error": "Unauthorized",
        "exception": "dk.magenta.datafordeler.core.exception.InvalidTokenException",
        "message": "Token is older than 43200 seconds",
        "path": "/cpr/person/1/rest/search"
    }


404 - Not found - Ingen besked er modtaget
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

API'et svarer ikke på henvendelsen. Efterse i dokumentation om API'et findes eller ej. Hvis API er korrekt, kan det være ude af drift, så vent lidt og forsøg igen.::

    {
        "timestamp": 1499421458146,
        "status": 404,
        "error": "Not Found",
        "message": "No message available",
        "path": "/cvr/1/rest/"
    }

