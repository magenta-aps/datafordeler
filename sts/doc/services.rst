.. _sts-services:

Services i DAFO STS
############################################

DAFO STS er en Secure Token Service der genererer tokens til brug i
den grønlandske datafordeler.

Tokens udstedes ud fra data i en database administreret via
`DAFO Admin <https://github.com/magenta-aps/datafordeler-admin>`_
og anvendes dels til at give adgang til den grønlandske
`datafordeler <https://github.com/magenta-aps/datafordeler-core>`_
og dels til at logge ind i
`DAFO Admin <https://github.com/magenta-aps/datafordeler-admin>`_ med.

Løsningen består af tre `Maven <https://maven.apache.org/>`_ projekter,
``dafo-sts-library``, ``dafo-sts-clientcertificates`` og ``dafo-sts-saml``,
samlet i det overordende Maven-projekt ``dafo-sts``.

**dafo-sts-library** indeholder klasser og interfaces der er fælles for de
to andre underprojekter.

**dafo-sts-clientcertificates** er en
`Spring Boot <https://projects.spring.io/spring-boot/>`_
løsning der gør det muligt at udstede tokens ud fra klient-certifikater
udstedt fra
`DAFO Admin <https://github.com/magenta-aps/datafordeler-admin>`_.

**dafo-sts-saml** er en
`Spring Boot <https://projects.spring.io/spring-boot/>`_
løsning der gør det muligt at udstede tokens via brugernavn og password
eller via bootstrap tokens fra en ekstern IdP konfigureret i
`DAFO Admin <https://github.com/magenta-aps/datafordeler-admin>`_.

Webservices
===========
De to Spring Boot underprojekter udstiller en række webservices der
facilitrer de forskellige måder at udstede tokens på.

Webservices i ``dafo-sts-cliencertificates``
--------------------------------------------
Adgang til alle services i ``dafo-sts-cliencertificates`` kræver at
klienten identificerer sig via et klient-certificat udstedt fra
DAFO Admin.

Der findes følgende services:

``/get_token``
  Returnerer en DAFO token i response body'en hvis
  klienten identificerer sig selv via et certifikat udstedt fra DAFO Admin.

Webservices i ``dafo-sts-saml``
-------------------------------
Alle services placeret under ``/by_saml_sso/`` på ``dafo-sts-saml`` serveren
er beskyttet af en `SAML2 <https://en.wikipedia.org/wiki/SAML_2.0>`_
login-løsning der gør det muligt at logge ind via Identity Providuers
der er defineret i DAFO-admin.

Der findes følgende services:

``/idpselection``
  En interaktiv HTML-side der giver mulighed for at vælge hvilken
  IdentityProvider der skal anvendes i forbindelse med SAML2 SSO login.
  Sender automatisk brugeren videre til forvalgt IdP, hvis en sådan er
  blevet valgt ved brug af ``sso_proxy`` servicen.

  **Output**:
    En HTML-side med en formular der giver mulighed for at vælge IdP.

``/get_metadata``
  Giver mulighed for at hente Identity Provider metadata XML for STS'en.

  **Output**:
    En xml fil med metadata for STS'en som token issuer.

``/get_token_passive``
  Giver mulighed for at udstede en token ved angivelse af brugernavn
  og password eller ved at angive en bootstrap token fra en
  registreret ekstern IdP.

  **Parametre**:
    ``username``
      Brugernavn på en bruger opretter i DAFO Admin. Anvendes sammen
      med password.
    ``password``
      Kodeord for en bruger oprettet i DAFO Admin. Andenves sammen
      med username.
    ``token``
      Bootstrap token fra en ekstern IdP der er registreret i DAFO
      Admin.

    Servicen kan anvendes enten ved at angive ``username`` og
    ``password`` eller ved at angive en valid ``token``.

  **Output**:
    En base64-encoded og deflated SAML2 token udstedt af STS'en.

``/by_saml_sso/get_token``
  Udsteder en token til den bruger der er logget ind via SAML SSO.

  **Output**:
    En base64-encoded og deflated SAML2 token udstedt af STS'en.

``/by_saml_sso/get_token_for_service``
  Udsteder en token til den bruger der er logget ind via SAML SSO
  og placerer den i en HTML-formular der sender den videre til en
  angivet URL via HTTP POST.
  Bruges som en del af ``/sso_proxy`` løsningen.

  **Output**:
    En HTML-side der sender en base64-encoded og deflated SAML2
    token videre til en angivet URL.


``/sso_proxy``
  Giver mulighed for at få udstedt en token via SAML SSO login og
  få den sendt til en angivet URL via HTTP POST. Bruges til login
  i DAFO Admin.

  Fungerer ved at gemme oplysninger om ønsket destinationsURL,
  forvalgt IdP og retur query-parameter i brugerens session og
  viderestille til ``/by_saml_sso/get_token_for_service`` der
  sørger for at udstede token og sende den tilbage til det
  angivne.

  **Parametre**:
    ``dafo_ssoproxy_url``
      Den URL den udstedte token skal sendes til.
    ``dafo_ssoproxy_idp`` (valgfri)
      IdentityID på den IdP der ønskes anvendt i forbindelse med
      SAML login. Angives denne parameter ikke præsenteres
      brugeren for IdP-valg på ``/idpselection`` som en del
      af login-processen.
    ``dafo_ssoproxy_returnparam`` (valgfri)
      Den HTTP POST parameter der bruges til at sende token tilbage
      til den angivne URL. Angives dette parameter ikke bruges
      værdien ``token``.
