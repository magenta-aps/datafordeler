from lxml import etree

import base64
import datetime
import os
import re
import requests
import zlib

endpoint = "dafo-sts-default-entity-id"
username = "amalie@serviceudbyder.gl"
password = "amalie"

utcnow = datetime.datetime.utcnow()
expire = utcnow + datetime.timedelta(minutes=5)

REQUEST_DATA = {
    "endpoint": endpoint,
    "created": utcnow.isoformat()[:23] + "Z",
    "expires": expire.isoformat()[:23] + "Z",
    "username": username,
    "password": password,
}

BASE_DIR = os.path.dirname(__file__)
TEMPLATE_FILE = os.path.join(BASE_DIR, "templates", "requestsecuritytoken.xml")


def replace_vars(match):
    return REQUEST_DATA.get(match.group(1), "asdf")


def expand_template(input):
    return re.sub(r"\{\{([^}]+)\}\}", replace_vars, input)


if __name__ == '__main__':
    f = open(TEMPLATE_FILE)
    xml = f.read()
    f.close()

    xml = expand_template(xml)

    idp_url = "https://dafo-idp.magenta.dk/services/wso2carbon-sts"

    resp = requests.post(
        idp_url,
        data=xml,
        verify=True,
        headers={'Content-Type': 'application/soap+xml; charset=utf-8'},
    )

    doc = etree.fromstring(resp.content)

    if doc.find('./{*}Body/{*}Fault') is not None:
        print resp.content
        raise Exception(' '.join(doc.itertext('{*}Text')))

    tokens = doc.findall('.//{*}RequestedSecurityToken/{*}Assertion')


    print etree.tostring(tokens[0], pretty_print=True)
    print "\n======\n"

    xml_str = etree.tostring(tokens[0])
    print base64.b64encode(zlib.compress(xml_str.encode('utf-8'), zlib.Z_DEFAULT_COMPRESSION)[2:])
