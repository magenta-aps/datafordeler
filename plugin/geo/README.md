GEO
============

This project fetches information from GAR.

Data for this module is fetched daily.

The Greenlandic roadregiser is fetched daily from the GAR-server.
The roadregister contains information about how a roadcode and munipialicitycode can be translated into a readable roadname.
This register is used for generating readable Danish adresses in datafordeleren.



I dette plugin udstilles services for brug af digital flytning

Hermed en beskrivelse:


Digital flytning kan kalde denne service for at finde lokaliteter i en kommune:
https://dafo.data.gl/geo/adresse/lokalitet/?kommune=###
Her leveres en liste af lokaliteter i den givne kommune, dette er lavet uden kompenceringer for problematiske data i GAR.
Der leveres en liste over lokaliteter og UUID'er på disse


Derefter kan de finde veje i Lokaliteten:
https://dafo.data.gl/geo/adresse/vej?lokalitet=UUID

Der leveres følgende attributter:
"navn"
"andet_navn"
"vejkode"
"kommunekode"
"uuid"


Ovenstående services er der ingen grund til at bruge tid på, det er bare baggrundsinformation



Herefter kan man fremfindes huse på vejen, dette gøres ved at kalde med vejens UUID

/geo/adresse/hus?vej=UUID

Der leveres følgende attributter i responset:
        "b_nummer"
        "husnummer"
        "b_kaldenavn"
       
Bygningsnummer "b_nummer" får fjernet "B-" i responset
Hvis der er flere bygningsnumre med samme husnummer frasorteres de i responset


       
Eller adresser på vejen

/geo/adresse/adresse?vej=UUID
Yderligere søgeparametre: "husnr", "b_nummer";

Der leveres følgende attributter i responset:
        "uuid"
        "b_nummer"
        "husnummer"
        "etage"
        "doer"
        "b_kaldenavn"
        "anvendelse"

       
       
Bygningsnummer "b_nummer" får fjernet "B-" i responset
Hvis der er flere bygningsnumre med samme husnummer frasorteres de i responset


Ved fremsøgning på "husnr" fjernes foranstillede nuller og der laves fremsøgning både med og uden disse nuller.
Dermed kan adresser findes uanset om der er foranstillede nuller

Ved fremsøgning på b_nummer appendes "B-" foran "b_nummer" (B-numre er altid angivet med B- i GAR)
Der appendes A,B,C,D,E,F efter bygningsnummeret (Og søges på alle kombinationer) (Dermed fremfindes også bygninger som hedder B-304A)

Ved output af "b_nummer" fjernes "B-"

Dør fra GAR populeres i attributten "doer" men
hvis der ikke er nogen værdi i GAR's dør, og bygningsnummeret ender med et bogstav, så leveres dette bogstav i "doer"




Der er også en service for opslag på en specifik adresse på baggrund af dennes uuid, men det er der ikke grund til at fortælle mere om

/geo/adresse/adresseoplysninger?adresse=uuid


Sammenfattede kompenseringer:
B-numre i responses til digital flytning angives altid uden "B-" og eventuelt bogstav i slutningen.

Dvs. at "B-" fjernes inden b-nummeret leveres


Ved fremsøgning på "husnr" fjernes foranstillede nuller og der laves fremsøgning både med og uden disse nuller.
Dermed kan adresser findes uanset om der er foranstillede nuller

Ved fremsøgning på b_nummer appendes "B-" foran "b_nummer" (B-numre er altid angivet med B- i GAR)
Der appendes A,B,C,D,E,F efter bygningsnummeret (Og søges på alle kombinationer) (Dermed fremfindes også bygninger som hedder B-304A)


Dør fra GAR populeres i attributten "doer" men
hvis der ikke er nogen værdi i GAR's dør, og bygningsnummeret ender med et bogstav, så leveres dette bogstav i "doer"
