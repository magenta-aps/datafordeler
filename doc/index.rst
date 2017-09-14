STS Secure Token Service
========================

De beskyttede personfølsomme data er kun tilgængelig for godkendte og aftalte systemer! Alle brugere være kendt og de er ansvarlige for deres anvendelse af systemet. Alt aktiviteter logges i henhold til lovgivningen om personfølsomme data - Her kan du læse mere om hvordan man får adgang til aftalte data, efter at din virksomhed har lavet aftale  med den grønlandske digitaliseringsstyrelse og de aktuelle dataejere.

.. toctree::

   sts-access-rights
   sts-usage


Introduktion til token
----------------------

I den grønlandske datafordeler https://www.data.gl skal alle tilgange udføres med SAML2-token, når der er behov for autorisation og autentifikation. Disse vejledninger skal hjælpe tredjeparter i gang med at forberede hvordan de kan anvende systemet. 

Hvordan et anvender-system sættes op og stiller forespørgsler, findes beskrevet i [[API forbindelse]].

Sammenhængen mellem STS og brugere, profiler samt roller er beskrevet i dokumentationen [[STS og hvordan datafordeleren anvender det]].


Brugernavn og kodeord?
----------------------

En almindelig reaktion er at det er besværligt at komme på systemet med personfølsomme data. Så her er en kort beskrivelse af årsagen til sikkerhedssystemet.


Hvorfor kan jeg ikke bare få et brugernavn og et kodeord?
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

* Det korte svar er at brugernavn og kodeord er alt for usikkert til fortrolige data - Det er ubrugeligt!
* Det er af samme årsag som der anvendes personlige og sikker login på borgerportaler som for eksempel sullissivik.gl.


Hvad er så fordelene ved token-systemet?
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

* Datafordeleren samler data fra mange forskellige myndigheder. Hver enkelt af disse myndigheder har krav på at få præcise navne på hvem der har set hvilke af deres data og hvornår.
* Både bruger og datansvarlig myndighed har krav til at registrering af brugeren og dennes aktivitet.
* Der skal være sikkerhed for at brugeren virkelig er den rigtige levende person, som retsforfølges ved misbrug.
* Misbrug af data er strafbart og med sikkerhedssystemet er der sikkerhed for at den registrerede bruger også er den som straffes ved misbrug.
* Hver bruger arbejder på vegne af sin arbejdsplads. Arbejdspladsen sikrer at brugeren er berettiget til at se de fortrolige data. Systemet sikrer at arbejdspladsen har godkendt adgang. 
* Arbejdspladsen garanterer for at det nu også er den rigtige person som er bag brugeren på datafordeler, så hvis arbejdspladsen beviseligt misbruger brugers oplysninger, straffes arbejdspladsen og dens ledelse hårdt på baggrund af beviserne i systemet.
* Rettigheder til de enkelte dele af data oprettes ikke centralt, men af  de myndigheder som har ansvaret for de enkelte data. Det er muligt at styre med sikkerhedssystemet.
* Bruger forhindres i at kunne videregive sine oplysninger til andre, eller sætte brugernavn og kodeord på sin skærm, så andre kan bruge det.

Det er kun personfølsomme data, der er beskyttet. Alle andre data er åbne for alle, der kan trække på dem uden at de skal registreres på forhånd.
