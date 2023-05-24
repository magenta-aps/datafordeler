#!/bin/python3
import concurrent.futures
import json
import os.path
import sys
import traceback
from datetime import date
from pyxlsx import open_xlsx

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
    adresse = result.get("adresse")
    status = None
    if result.get("civilstand") == "D":
        status = f"Død {result.get('civilstandsdato', '')}"
    return (komkod, vejkod, postnr, adresse, status)


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
        if not "adresse" in generic_data:
            return None, None
        for adresse in generic_data["adresse"]:
            if komkod == adresse["kommunekode"] and vejkod == adresse["vejkode"]:
                husnummer = adresse["husnummer"]
                if not husnummer:
                    return adresse["postnummer"], husnummer
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
    komkod, vejkod, husnr, adresse, postnr = None, None, None, None, None
    try:
        if len(nr) == 11:
            nr = nr[0:6] + nr[7:11]
        if not is_cpr(nr) and not is_cvr(nr):
            return komkod, vejkod, husnr, postnr, adresse, f"{nr} er hverken et CPR- eller CVR-nummer"

        # Mulighed: Prisme giver os ikke vejkode og kommunekode
        komkod, vejkod, postnr, adresse, status = get_prisme_road(nr)
        if status:
            return komkod, vejkod, husnr, postnr, adresse, status

        if not komkod or not vejkod:
            return komkod, vejkod, husnr, postnr, adresse, f"Fik ikke kommunekode/vejkode fra kald til Dafo-prisme"

        # Mulighed: Opslaget giver os faktisk et postnummer
        if postnr:
            return komkod, vejkod, husnr, postnr, adresse, f"Har postnummer ({postnr})"

        # Mulighed: Vejkode og kommunekode findes ikke
        road = get_road(komkod, vejkod)
        if not road:
            return komkod, vejkod, husnr, postnr, adresse, f"Mangler vej i geodata (komkod: {komkod} / vejkod: {vejkod})"

        postnr, husnr = get_address(nr, komkod, vejkod)
        if not husnr:
            return komkod, vejkod, husnr, postnr, adresse, f"Adressen har ikke noget husnummer (komkod: {komkod} / vejkod: {vejkod})"
        if not postnr:
            return komkod, vejkod, husnr, postnr, adresse, f"Mangler adgangsadresse i geodata (komkod: {komkod} / vejkod: {vejkod} / husnr: {husnr})"
        return komkod, vejkod, husnr, postnr, adresse, "OK"
    except Exception as e:
        print(f"{type(e)} {e}")
        traceback.print_exc()
        return komkod, vejkod, husnr, postnr, adresse, "Fejl"


def scan_worksheet_row(index, row):
    if row["COUNTRYREGIONID"] != "GRL":
        row.update({"Status": "Udenfor Grønland"})
        return row
    number_cell = row["CPR_CVR"]
    nr = str(number_cell)
    if len(nr) == 9:
        nr = "0" + nr
    kommunekode, vejkode, husnr, postnummer, adresse, status = scan(nr)
    row.update({"Kommunekode": kommunekode, "Vejkode": vejkode, "Husnummer": husnr, "Postnummer": postnummer, "CPR-adresse": adresse, "Status": status})
    return row


if __name__ == "__main__":
    input_filename = sys.argv[1]
    certificate = sys.argv[2]
    private_key = sys.argv[3]
    output_filename = sys.argv[4]
    if not os.path.exists(input_filename):
        print(f"Input file {input_filename} does not exist")
        exit(1)
    if not os.path.isfile(input_filename):
        print(f"Input file {input_filename} is not a file")
        exit(1)
    session = create_session(certificate, private_key)

    with open_xlsx(input_filename) as wb:
        wb.filename = output_filename
        for sheet in wb.worksheets:
            sheet.header_row = 1
            with concurrent.futures.ThreadPoolExecutor(max_workers=10) as executor:
                updated_rows = {
                    executor.submit(scan_worksheet_row, index, row)
                    for index, row in enumerate(sheet.content_rows)
                }
                for future in concurrent.futures.as_completed(updated_rows):
                    result = future.result()
        wb.save(filename=output_filename)
