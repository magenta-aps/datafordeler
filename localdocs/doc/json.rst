.. _json-results:

Resultater af forespørgsler
===========================

JSON er et almindeligt tekstformat med forud-bestemte definitioner. Det er et format som er nemt for systemer at :ref:`parse` og udtrække de netop oplysninger, som brugeren skal anvende.

Resultatet af en forespørgsel er en JSON-tekst, der ser ud som følger::

    {
        "path": "/najugaq/municipality/1/search",
        "terms": "https://doc.test.data.gl/terms",
        "requestTimestamp": "2017-06-30T04:28:58.986-02:00",
        "responseTimestamp": "2017-06-30T04:28:59.049-02:00",
        "username": "[cp______er@magenta.dk]@[Na_________er]",
        ...
        "results: [
        ... ]
    }

* "path": fortæller kilden til informationerne og dermed  kender man også søgetypen, der har været anvendt.
* "terms": er link til de betingelser og regler, der gælder for data fra datafordeleren. De ses også på siden med :ref:`Betingelser. <agreements>`
* "requestTimestamp": er tidspunktet for hvornår serveren fik henvendelsen
* "responseTimestamp": er tidspunktet for hvornår serveren afleverede resultatet
* "username": er identifikationen for den anvender, som har bedt om data. Det vil være tomt, hvis brugernavn ikke er anvendt.
* ... Listen fortsætter med flere oplysninger om forespørgselen og her kan også findes fejlbeskeder.
* "results:" starter resultaterne for søgningen. 
* ... Indholdet og nøgleord vil variere efter søgningerne, der har været anvendt. Søgemulighederne er beskrevet i den fælles deklaration for API'ere på :ref:`API-deklarationer. <api-declarations>`
