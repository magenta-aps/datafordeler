#!/bin/python3
import concurrent.futures
import json
import os.path
import sys
from datetime import date

from requests import Session

root_ca = os.path.join(os.path.dirname(__file__), "pitu_ca.cert")
client_header = "PITU/GOV/DIA/magenta_services"
url = "https://10.240.76.91/restapi/"


def create_session(certificate, private_key):
    session = Session()
    session.cert = (certificate, private_key)
    session.verify = root_ca
    return session


def pitu_get(endpoint, path, params=None):
    r = session.get(
        url + path,
        params=params,
        timeout=60,
        headers={"Uxp-client": client_header, "Uxp-service": endpoint},
    )
    r.raise_for_status()
    return r.json()


def pitu_post(endpoint, path, data=None):
    r = session.post(
        url + path,
        data=data,
        timeout=60,
        headers={"Uxp-client": client_header, "Uxp-service": endpoint},
    )
    r.raise_for_status()
    return r.json()


def get_prisme_road(nr):
    try:
        if is_cpr(nr):
            result = pitu_post(
                "PITU/GOV/DIA/magenta_services/DAFO-PRISME-CPR-COMBINED/v1",
                "",
                json.dumps({"cprNumber": nr}).encode("utf-8"),
            )[nr]
        else:
            result = pitu_post(
                "PITU/GOV/DIA/magenta_services/DAFO-PRISME-CVR-COMBINED/v1",
                "",
                json.dumps({"cvrNumber": nr}).encode("utf-8"),
            )[nr]
    except KeyError:
        result = {}
    komkod = result.get("myndighedskode")
    vejkod = result.get("vejkode")
    postnr = result.get("postnummer")
    return (komkod, vejkod, postnr)


def get_road(komkod, vejkod):
    result = pitu_get(
        "PITU/GOV/DIA/magenta_services/DAFO-ADDRESS-GENERIC/1",
        "road/1/rest/search",
        {"kommunekode": komkod, "kode": vejkod},
    )
    try:
        return result["results"][0]
    except (KeyError, IndexError):
        return None


def get_address(nr, komkod, vejkod):
    today = date.today().isoformat()
    if is_cpr(nr):
        results = pitu_get(
            "PITU/GOV/DIA/magenta_services/DAFO-CPR-GENERIC/1",
            f"search/?pnr={nr}&registrationToAfter={today}&effectToAfter={today}&format=dataonly",
        )
    else:
        results = pitu_get(
            "PITU/GOV/DIA/magenta_services/DAFO-CVR-GENERIC/1",
            f"search/?cvrNummer={nr}&registrationToAfter={today}&effectToAfter={today}&format=dataonly",
        )
    try:
        generic_data = results["results"][0]
    except (KeyError, IndexError):
        pass
    else:
        for adresse in generic_data["adresse"]:
            if komkod == adresse["kommunekode"] and vejkod == adresse["vejkode"]:
                husnummer = adresse["husnummer"]
                postnummer = None
                adresse_result = pitu_get(
                    "PITU/GOV/DIA/magenta_services/DAFO-ADDRESS-GENERIC/1",
                    "accessaddress/1/rest/search",
                    {"kommunekode": komkod, "vejkode": vejkod, "husNummer": husnummer},
                )
                try:
                    adresse = adresse_result["results"][0]
                    postnummer = adresse["postnummer"]
                except (KeyError, IndexError):
                    pass
                return postnummer, husnummer
    return None, None


def is_cpr(nr: str):
    return nr.isnumeric() and len(nr) == 10


def is_cvr(nr: str):
    return nr.isnumeric() and len(nr) == 8


def scan(nr):
    nr = nr.strip()
    try:
        if len(nr) == 11:
            nr = nr[0:6] + nr[7:11]
        if not is_cpr(nr) and not is_cvr(nr):
            print(f"Input {nr} er hverken et CPR- eller CVR-nummer")
            return

        # Mulighed: Prisme giver os ikke vejkode og kommunekode
        komkod, vejkod, postnr = get_prisme_road(nr)

        if not komkod or not vejkod:
            print(f"{nr}  Fik ikke kommunekode/vejkode fra kald til Dafo-prisme")
            return

        # Mulighed: Opslaget giver os faktisk et postnummer
        if postnr:
            print(f"{nr}  Har postnummer ({postnr})")
            return

        # Mulighed: Vejkode og kommunekode findes ikke
        road = get_road(komkod, vejkod)
        if not road:
            print(f"{nr}  Fik ikke vejnavn fra kald til geodata ({komkod}/{vejkod})")
            return

        postnr, husnr = get_address(nr, komkod, vejkod)
        if not husnr:
            print(f"{nr}  Fik ikke et husnummer fra kald til geodata ({komkod}/{vejkod})")
            return
        if not postnr:
            print(
                f"{nr}  Fik ikke en adgangsadresse fra kald til geodata ({komkod}/{vejkod}/{husnr})"
            )
            return
        print(f"{nr}  OK")
    except Exception as e:
        print(e)


if __name__ == "__main__":
    filename = sys.argv[1]
    certificate = sys.argv[2]
    private_key = sys.argv[3]
    if not os.path.exists(filename):
        print(f"Input file {filename} does not exist")
        exit(1)
    if not os.path.isfile(filename):
        print(f"Input file {filename} is not a file")
        exit(1)
    session = create_session(certificate, private_key)
    with open(filename, "r") as fp:
        lines = [line.strip() for line in fp.readlines()]
        with concurrent.futures.ThreadPoolExecutor(max_workers=10) as executor:
            executor.map(scan, lines)
