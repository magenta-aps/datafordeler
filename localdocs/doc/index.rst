Grønlands Datafordeler API
==========================

.. toctree::
   :maxdepth: 2
   :caption: Indhold

   api-declarations
      cpr-api-declarations
   api-connection
   api-results
   json

Anvendelse af API er underlagt [[Betingelser]] for anvendelse af Datafordeleren og dens formidlede data. API findes på https://test.data.gl/ 


API'er og deklarationer
-----------------------

Generelt gælder det at: 

* API'er er selvdeklarerende RESTfull API'er og de er alle af typen GET.
* Navngivning af URL til API følger hovedreglen: @/ et kort registernavn (f.eks. cpr) / et versionnummer f.eks. (1) / API-type (f.eks. rest) /@ og så følger parametrene. 
* Et kald til roden på de enkelte API'er giver en liste over parametre, der er tilgængelige i API'et.
* Uddybning af felter, navne, deres anvendelse og særlige betingelser findes hos de respektive myndigheder, der udstiller data via datafordeleren.

    * Eventuelle afvigelser og særlige forhold for Datafordeleren vil være nævnt i deklarationen.

* De resulterende svarformater er beskrevet på siden om formatet [[JSON]].


Et eksempel på deklarationskald til API
---------------------------------------

Et kald som@ https://test.data.gl/cpr/person/1/rest @ giver resultat som på billedet. Opsætningen kan variere fra browser til browser. !>API_selfdeclaration.png!

Selvdeklarationen af et API på datafordeleren rummer disse oplysninger:

* "metadata_url": Adressen på metadata om API'et
* "fetch_url": Adressen til at hente oplysninger ved personens unikke UUID, der alene anvendes i Datafordeleren"
* "search_url": Adressen til søgning på API'et
* "declaration_url": Web-adressen til [[betingelser]] for anvendelse af API'et
* "search_queryfields": Oversigten over søgefelter på det aktuelle API
* "name": Navn(ene) på de tilgængelige søgefelter
* "type": Datatypen der skal anvendes ved søgningen

Uddybning af feltnavnene og deres indhold samt anvendelse er dokumenteret hos de ansvarlige myndigheder for registret. Hvor det er muligt, vil der være link til myndighedens databeskrivelse. Eventuelle særlige forhold i forbindelse med Datafordeleren vil være nævnt i deklarationen af API'et.


Søgemønstre i API-søgninger
---------------------------

Søgning skrives i en parameterform som @search?"felt"="mønster"@.

* Alle tilgængelige objekter bliver gennemsøgt for mønstret i det nævnte søgefeltnavn. Der kan anvendes forskellige sammenligninger og mønstre 

================= ========================================================================== =====================================================================================================================
*Joker*           Anvendelse                                                                 Eksempel
================= ========================================================================== =====================================================================================================================
tegn              Finder alle felter med den givne værdi (mellemrum ˽ er også et tegn)       @fornavn=Malik@ finder alle med navn Malik - @fornavn= Malik˽@ (med mellemrum foran eller bagved) finder _ikke_ Malik
?                 Spørgsmålstegn repræsenterer nul, et eller mange vilkårlige tegn           @fornavn=Pa%uaq@ finder alt der begynder med pa og slutter med uaq, f.eks. Paninnguaq
_                 Understregning repræsenterer et tegn                                       @fornavn=a?e@ finder alt på tre bogstaver, starter med a og slutter med e, f.eks. Ane
[tegnliste]       repræsenterer et sæt (f.eks. [abc]) eller et række (f.eks. [a-g] af tegn   @fornavn=N[a-d]v[a-d]@ finder alle med netop de bogstaver i navnet, f.eks. Naja (men Nivi findes ikke)
[^tegnliste]      repræsenterer et sæt eller et række af tegn som skal udelades i søgningen  @fornavn=N[^a-d]v[^a-d]@ Finder alle uden bogstaverne a, b, c, d, f.eks. Nivi (men Naja findes ikke)
================= ========================================================================== =====================================================================================================================

================= ========================================================================== =====================================================================================================================
*Sammenligninger* Anvendelse                                                                 Eksempel
================= ========================================================================== =====================================================================================================================

=                 Lighedstegn finder alle som passer med mønsteret                           @fornavn=Benjamin@ finder netop Benjamin og ikke Malik, Zakæus eller andre
!=                Ulighedstegn finder allesom er forskellig fra mønsteret                    @fornavn!=Zakæus@ finder f.eks. Benjamin, Malik med flere men ikke Zakæus
&                 Ambersand kombinerer de to sammenligninger                                 @fornavn=Zakæus&efternavn=Petersen@ finder netop Zakæus Petersen
================= ========================================================================== =====================================================================================================================


Find mere om API'er på disse sider:
-----------------------------------

{{child_pages(depth=5)}}

{{include(Undertekst)}}