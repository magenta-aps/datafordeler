.. _command:

Kommandointerface
=================

Datafordeleren indeholder et simpelt HTTP-interface til eksekvering af kommandoer,
såsom at sætte en dataimport i gang. Det er hensigten at kommandoer skal igangsættes af
et adminstrationssystem, men med værktøjer som f.eks. `Postman <https://www.getpostman.com/>`_
eller `curl <https://curl.haxx.se/>`_ er det muligt at anvende dette interface direkte.

Der er adgangskontrol på dette interface, så en gyldig SAML-token for en bruger med adgang til
at udføre kommandoer skal sendes med.

Kommandointerfacet lytter på adressen ``https://<server>/command/{id}``, hvor følgende muligheder er tilgængelige:


POST
  Eksempel: POST /command/pull

  Starter en kommando. For øjeblikket er kun kommandoerne "pull" og "dump" tilgængelige,
  hvor "dump" er beregnet til intern testing. Kommandoen vil forvente parametre, som
  angives i forespørgslens body, f.eks. ``{"plugin":"cvr"}`` starter et pull for CVR-pluginet.
  Hvis kommandoen accepteres, sender servicen en status tilbage, som bl.a. indeholder et id.

  Igangsætning af en kommando med POST resulterer i at oplysninger om kommandoen gemmes i databasen, hvorefter
  servicen returnerer et HTTP-svar. En scheduleret proces i datafordeleren samler oplysningerne op fra databasen
  og udfører kommandoen.

GET
  Eksempel: GET /command/7

  Henter status for en kommando, så det kan kontrolleres om kørslen er gået godt, stadig kører, eller er fejlet.
  Svaret er indkapslet i et JSON-objekt, med følgende felter:

  - received: Hvornår kommandoen blev modtaget.
  - handled: Hvis kommandoen er kørt færdig, blev afbrudt, eller der opstod en fejl, indsættes tidspunktet her.
  - errorMessage: Hvis der er opstået en fejl, indsættes fejlbeskeden her.
  - id: Kommandoens identifikation.
  - status: Kørslens status; en af ["queued", "running", "successful", "failure", "cancelled"].
  - commandName: Kommandoens navn, f.eks. "pull".


DELETE
  Eksempel: DELETE /command/7

  Afbryder en kørende kommando og returnerer status som i GET. Rent praktisk udføres dette ved at kommandoen
  findes frem i databasen, og statusfeltet sættes til en værdi som signalerer at udførelsen skal stoppe.
  En scheduleret proces i datafordeleren samler op på dette, finder den tilhørende kørende proces, og signalerer
  at der skal afbrydes.


At databasen bruges som mellemled har den fordel at der kan sendes et svar tilbage til forespørgeren, uden at
arbejdsprocessen bliver stoppet, hvilket ellers ville ske hvis arbejdsprocessen var startet af
HTTP-forespørgselshåndteringen. Desuden, idet kommandoer lagres i en database som er delt med DAFO-admin, kan
kommandoer også oprettes i databasen af DAFO-admin i stedet for Kommandointerfacet, og den schedulerede proces
opsamler kommandoerne uanset oprindelse.
