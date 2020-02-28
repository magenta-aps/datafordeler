Filhåndtering
=============

Data fordeleren tilbyder grundlæggende funktionalitet til
filhåndtering. Hver nat klokken to foretages et udtræk fra databasen,
og de vises på::

  /dump/list

Alle datatyper fra alle plugins gemmes automatisk. Det er muligt at
begrænse rettighederne, så udtræk ikke er tilgængelige for alle
brugere. Udtræk gemmes i følgende formater:

* JSON
* XML
* CSV, eller kommasepareret liste.
* TSV, eller tabsepareret liste.

Vi gemmer indtil videre kun det seneste udtræk.
