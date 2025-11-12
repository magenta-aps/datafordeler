Datafordelermotor
=================

Dokumentation for `datafordelermototen. <https://github.com/magenta-aps/datafordeler-core>`_

.. toctree::

    bitemporality.rst
    plugins.rst
    pull.rst


Formål
------

Datafordelerens Core-komponent har til opgave at styre import og udstilling af data, ved at:

- Implementere generelle metoder til import og udstilling
- Lade plugins definere de specifikke indstillinger (såsom adgangsroller, kilde-urls, og hvordan data hentes og fortolkes)
- Definere hvordan bitemporalitetsmodellen er opbygget
- Definere superklasser til plugins, som de som minimum skal implementere
- Stille hjælpeklasser og -metoder til rådighed for plugins
- Foretage databaseopslag til lagring og udtræk af dataentiteter


Komponenter
-----------

De væsentligste komponenter i Core er:

FapiService
  Stiller en FAPI-service superklasse til rådighed for plugins.
  Denne superklasse implementerer opslag generisk, og behøver blot enkelte definitioner fra hvert plugin.

Envelope
  Generel data-konvolut som vi anbringer svar på forespørgsler i, samt metadata omkring forespørgslen og svaret på den.

Query
  Definerer en standardiseret måde at beskrive databaseopslag.
  Ud fra brugerens forespørgsel opbygges et Query, som med en LookupDefinition oversætter de adspurgte felter til en databaseforespørgsel.

CommandService
  Stiller et kommando-interface til rådighed i form af en service,
  hvortil der kan sendes HTTP POST-forespørgsler for at eksekverer kommandoer (f.eks. Pull),
  GET for at se status for eksekverende kommandoer, og DELETE for at afbryde kommandoer.
  Se :ref:`command`.

ConfigurationManager
  Indlæser/opretter konfigurationsobjekter for de plugins som har en implementation af den.

Bitemporalitetsmodel
  Klasser til at definere bitemporalitetsmodellen overfor plugins.
  Se :ref:`bitemporality`.

LookupDefintion
  Klasse til at beskrive et opslag i databasen.
  Ud fra en liste af input for feltets objektsti, feltets datatype, den efterspurgte værdi,
  og eventuelt sammenlignings-operator, kan der genereres komponentstrenge til HQL-opslag i databasen.

QueryManager
  Fællespunkt for opslag i databasen; indeholder metoder til bl.a. at finde entiteter ud fra klasse,
  query og UUID, samt mere generelle opslagsmetoder.

Exception-samling
  Hierarkisk samling af exception-klasser til brug i datafordeleren;
  alle exception-klasser nedarver fra en abstrakt DatafordelerException, og har en unik kode.

GapiServlet
  Adgangspunkt for Push af data til datafordeleren.

Plugin-definition
  Superklasser til plugins. Definerer hvilke metoder og klasser et plugin som minimum skal implementere.
  Se :ref:`plugins`.

Hjælpeklasser til plugins
  Klasser til hentning af data fra eksterne kilder med HTTP, FTP, og HTTP med scan-scroll.

Rollesystem
  Klasser til brug for definition af hvilke brugerroller der findes i systemet.
  De enkele plugins skal instantiere og returnere disse i deres rolledefinition.

Brugerhåndtering
  Klasser til parsing, validering og håndtering af tokens som kommer ind med forespørgsler.
