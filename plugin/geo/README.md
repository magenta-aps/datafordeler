GEO
============

This project fetches information from GAR.

Data for this module is fetched daily.

The Greenlandic roadregister is fetched daily from the GAR-server. The roadregister contains information about how a
roadcode and munipialicitycode can be translated into a readable roadname. This register is used for generating readable
Danish adresses in datafordeleren.

I dette plugin udstilles services for brug af digital flytning

## Beskrivelse:

### Lokaliteter fra kommune

Digital flytning kan kalde denne service for at finde lokaliteter i en kommune:
https://dafo.data.gl/geo/adresse/lokalitet/?kommune=###
Her leveres en liste af lokaliteter i den givne kommune, dette er lavet uden kompenseringer for problematiske data i
GAR. Der leveres en liste over lokaliteter og UUID'er på disse

### Veje fra lokalitet

Derefter kan de finde veje i Lokaliteten:
https://dafo.data.gl/geo/adresse/vej?lokalitet=UUID

Der leveres følgende attributter:
"navn"
"andet_navn"
"vejkode"
"kommunekode"
"uuid"

Ovenstående services er der ingen grund til at bruge tid på, det er bare baggrundsinformation

### Hus fra vej

Herefter kan man fremfindes huse på vejen, dette gøres ved at kalde med vejens UUID

/geo/adresse/hus?vej=UUID

Der leveres følgende attributter i responset:
"b_nummer"
"husnummer"
"b_kaldenavn"

Bygningsnummer "b_nummer" får fjernet "B-" i responset Hvis der er flere bygningsnumre med samme husnummer frasorteres
de i responset

### Adresser fra vej

Eller adresser på vejen

/geo/adresse/adresse?vej=UUID Yderligere søgeparametre: "husnr", "b_nummer";

Der leveres følgende attributter i responset:
"uuid"
"b_nummer"
"husnummer"
"etage"
"doer"
"b_kaldenavn"
"anvendelse"

Bygningsnummer "b_nummer" får fjernet "B-" i responset Hvis der er flere bygningsnumre med samme husnummer frasorteres
de i responset

Ved fremsøgning på "husnr" fjernes foranstillede nuller og der laves fremsøgning både med og uden disse nuller. Dermed
kan adresser findes uanset om der er foranstillede nuller

Ved fremsøgning på b_nummer tilføjes "B-" foran "b_nummer" (B-numre er altid angivet med B- i GAR)
Der appendes A,B,C,D,E,F efter bygningsnummeret (Og søges på alle kombinationer) (Dermed fremfindes også bygninger som
hedder B-304A)

Ved output af "b_nummer" fjernes "B-"

Dør fra GAR populeres i attributten "doer" men hvis der ikke er nogen værdi i GAR's dør, og bygningsnummeret ender med
et bogstav, så leveres dette bogstav i "doer"

### Adresserdetaljer

Der er også en service for opslag på en specifik adresse på baggrund af dennes uuid, men det er der ikke grund til at
fortælle mere om

/geo/adresse/adresseoplysninger?adresse=uuid

Sammenfattede kompenseringer:
B-numre i responses til digital flytning angives altid uden "B-" og eventuelt bogstav i slutningen.

Dvs. at "B-" fjernes inden b-nummeret leveres

Ved fremsøgning på "husnr" fjernes foranstillede nuller og der laves fremsøgning både med og uden disse nuller. Dermed
kan adresser findes uanset om der er foranstillede nuller

Ved fremsøgning på b_nummer appendes "B-" foran "b_nummer" (B-numre er altid angivet med B- i GAR)
Der appendes A,B,C,D,E,F efter bygningsnummeret (Og søges på alle kombinationer) (Dermed fremfindes også bygninger som
hedder B-304A)

Dør fra GAR populeres i attributten "doer" men hvis der ikke er nogen værdi i GAR's dør, og bygningsnummeret ender med
et bogstav, så leveres dette bogstav i "doer"

## Hvad vi henter fra GAR

Hermed en beskrivelse af hvilke data vi henter fra GAR, denne information kan benyttes i forbindelse med kommunikation
med COWI og Digitaliseringsstyrelsen

#### Atributter som gemmes for alle objekter

OBJECTID, GlobalID, sumiffiikId, creator, CreationDate, Editor, EditDate

Herudover gemmes attributter specifikt for de enkelte objekter.

Der indlæses også GIS-koordinater, men dem bruger vi ikke til noget

### Kommuner

https://nogd01.knno.local/server/rest/services/OperationalLayers/Grunddata/FeatureServer/4/query?where=EditDate>'%{editDate}'&outFields=*&returnGeometry=true&resultOffset=%{offset}&resultRecordCount=%{count}&f=geojson

#### Atributter som gemmes for kommuner

Kommunenavn, Kommunekode, groupid

### Lokaliteter

https://nogd01.knno.local/server/rest/services/OperationalLayers/Grunddata/FeatureServer/3/query?where=EditDate>'%{editDate}'&outFields=*&returnGeometry=true&resultOffset=%{offset}&resultRecordCount=%{count}&f=geojson

#### Atributter som gemmes for Lokaliteter

Lokalitetsnavn, Lokalitetsnavn_forkortelse, Lokalitetskode, Kommunekode, LokalitetSumiffik, Lokalitetvejkode,
Location_type

### Lokalitetssletninger

https://nogd01.knno.local/server/rest/services/OperationalLayers/Grunddata/FeatureServer/10/query?where=DeletedDate%3E'%{editDate}'&f=json&outFields=*&resultOffset=%{offset}&orderByFields=DeletedDate

### Veje

https://nogd01.knno.local/server/rest/services/OperationalLayers/Grunddata/FeatureServer/1/query?where=EditDate>'%{editDate}'&outFields=*&returnGeometry=true&resultOffset=%{offset}&resultRecordCount=%{count}&f=geojson

#### Atributter som gemmes for Veje

Vejnavn, Vejadresseringsnavn, Vejkode, Lokalitetskode, Kommunekode, VejmidteSumiffik")

### Vejsletninger

https://nogd01.knno.local/server/rest/services/OperationalLayers/Grunddata/FeatureServer/11/query?where=DeletedDate%3E'%{editDate}'&f=json&outFields=*&resultOffset=%{offset}&orderByFields=DeletedDate

### Postnumre

https://nogd01.knno.local/server/rest/services/OperationalLayers/Grunddata/FeatureServer/5/query?where=Postnummer>0&outFields=*&resultOffset=%{offset}&resultRecordCount=%{count}&f=geojson

#### Atributter som gemmes for Postnumre

Postdistri, Postnummer

### Adgangsadresser

https://nogd01.knno.local/server/rest/services/OperationalLayers/Grunddata/FeatureServer/0/query?where=EditDate>'%{editDate}'&outFields=*&returnGeometry=true&resultOffset=%{offset}&resultRecordCount=%{count}&f=geojson

#### Atributter som gemmes for Adgangsadresser

BNummer, LokalitetsKode, KommuneKode, BlokNavn, ObjektStatus, HusNummer, Postnummer, Vejkode, DataKilde, bygning_id,
AdgangsadresseSumiffik

### Adgangsadressesletninger

https://nogd01.knno.local/server/rest/services/OperationalLayers/Grunddata/FeatureServer/7/query?where=DeletedDate%3E'%{editDate}'&f=json&outFields=*&resultOffset=%{offset}&orderByFields=DeletedDate

### Enhedsadresser

https://nogd01.knno.local/server/rest/services/OperationalLayers/Grunddata/FeatureServer/6/query?where=EditDate>'%{editDate}'&outFields=*&returnGeometry=false&resultOffset=%{offset}&resultRecordCount=%{count}&f=geojson

#### Atributter som gemmes for Enhedsadresser

access_adress_id, Enhedsnummer, Enhedsanvendelse, Etage, Dor_lejlighedsnummer, Objektstatus, Datakilde,
EnhedsadresseSumiffik

### Enhedsadressesletninger

https://nogd01.knno.local/server/rest/services/OperationalLayers/Grunddata/FeatureServer/9/query?where=DeletedDate%3E'%{editDate}'&f=json&outFields=*&resultOffset=%{offset}&orderByFields=DeletedDate

### Bygningsnumre

https://nogd01.knno.local/server/rest/services/OperationalLayers/Grunddata/FeatureServer/2/query?where=EditDate>'%{editDate}'&outFields=*&returnGeometry=true&resultOffset=%{offset}&resultRecordCount=%{count}&f=geojson

#### Atributter som gemmes for Bygningsnumre

a_nummer, B_nummer, location_id, BygningSumiffik

### Bygningsnummersletninger

https://nogd01.knno.local/server/rest/services/OperationalLayers/Grunddata/FeatureServer/8/query?where=DeletedDate%3E'%{editDate}'&f=json&outFields=*&resultOffset=%{offset}&orderByFields=DeletedDate
