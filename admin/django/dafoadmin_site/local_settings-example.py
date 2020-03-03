# -*- coding: utf-8 -*-
# This file contains example configuration that can be used to customize how
# the dafo-admin site behaves. Not all options are neccessary to specify,
# but they are provided here with some reasonable defaults.
import os

BASE_DIR = os.path.dirname(os.path.dirname(__file__))

# Connecting to an MSSQL database using windows authentication
DATABASES = {
    # The default database, containing the user data
    'default': {
        'NAME': 'dafo_users',
        'ENGINE': 'sqlserver_ado',
        'HOST': 'localhost',
        'OPTIONS': {
            'provider': 'SQLNCLI11',
            'use_legacy_date_fields': 'True',
            'extra_params': 'Trusted_Connection=yes'
        }
    },
    # Secondary database containing the configuration for the
    # different plugins in the datafordeler.
    'configuration': {
        'NAME': 'dafo_data',
        'ENGINE': 'sqlserver_ado',
        'HOST': 'localhost',
        'OPTIONS': {
            'provider': 'SQLNCLI11',
            'use_legacy_date_fields': 'True',
            'extra_params': 'Trusted_Connection=yes'
        }
    }
}

# Logging setup that logs both to the console and to the windows event log
# Note that it will be needed to run the application as administrator one time
# in order to register the name of the Windows logging handler. See the
# 'add_to_registry' option below for more information.
LOGGING = {
    'version': 1,
    'disable_existing_loggers': False,
    'handlers': {
        # Log to the console
        'console': {
            'class': 'logging.StreamHandler',
        },
        # Log to Window's Event log
        'eventlog': {
            'level': 'INFO',
            'class': 'dafousers.logginghandlers.NoAdminNTEventLogHandler',
            'appname': 'DAFO-Admin',
            # Set this to True and run the server as administrator to
            # do a one-time registration for the Windows Event Log.
            'add_to_registry': False
        },
    },
    'loggers': {
        'django': {
            'handlers': ['console', 'eventlog'],
            'level': 'INFO',
            'propagate': True,
        },
    },
}

# Path to keystore containing certificate and key for signing client
# certificates
CERT_ROOT = os.path.join(os.path.dirname(BASE_DIR), "local")
ROOT_CERT_NAME = "our_root_CA.jks"
# Password used to access the .jks keystore
ROOT_CERT_PASS = 'passwordForRootCA'
# The alias under whiche the root CA is stored in the keystore
ROOT_CERT_ALIAS = 'root_ca_alias'

# The URL we use for logging in via the IdP
IDP_SSOPROXY_URL = "https://sts.dafo.example.com/sso_proxy"

# How many seconds time skew to allow when checking SAML tokens
MAX_SAML_TIME_SKEW = 60 * 10

# How many seconds to allow a token to live before we reject it
MAX_SAML_TOKEN_LIFETIME = 60 * 60 * 12

# SAML audience URI, must be the same as is added to tokens by the STS
SAML_AUDIENCE_URI = "https://dafo.example.com/"

# The issuer NameID used by the DAFA STS that issues the tokens
SAML_STS_ISSUER = "Example-Dafo-STS"

# The public x509 cert the STS uses to sign tokens
SAML_STS_PUBLIC_CERT = """\
MIIHYTCCBkmgAwIBAgIMZXTwKS8+25QBJw7QMA0GCSqGSIb3DQEBCwUAMGAxCzAJBgNVBAYTAkJF
MRkwFwYDVQQKExBHbG9iYWxTaWduIG52LXNhMTYwNAYDVQQDEy1HbG9iYWxTaWduIERvbWFpbiBW
YWxpZGF0aW9uIENBIC0gU0hBMjU2IC0gRzIwHhcNMTcwNjI4MTExNzAyWhcNMjAwNjIwMjEyNDEx
WjA3MSEwHwYDVQQLExhEb21haW4gQ29udHJvbCBWYWxpZGF0ZWQxEjAQBgNVBAMMCSouZGF0YS5n
bDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAM/+FuS5bI3uLS06cSCtlk1Ja3IAoy7p
VW+HuGiFC5EMlwGtbVMjeOQtzBuafKhQrd4QXTIw426aemCWOy6QK+xv/guEEY+OqOB0x6kPqddY
28LMesjzNXEi+vkEM7b1DDEuzZuC2pcjmqJfrXUVuvMCTsth5q9zXN5bFWmPjwK/OkET+FKZmK6b
r6310Ww0ZuZL3Y75URC4OE9V0TTUW4wfi8AFwckY1Qiz4UylgsAhu0L0s411g38uJPXjDeQ7LZ8m
ZiblfC7p6jHTnKXNXyJqOTB0D+z4TWgfg+a7hso+eZew0uNFu7GSMBHJgASC68rG0YIf+Tqb2cmy
fY3OruUCAwEAAaOCBEIwggQ+MA4GA1UdDwEB/wQEAwIFoDCBlAYIKwYBBQUHAQEEgYcwgYQwRwYI
KwYBBQUHMAKGO2h0dHA6Ly9zZWN1cmUuZ2xvYmFsc2lnbi5jb20vY2FjZXJ0L2dzZG9tYWludmFs
c2hhMmcycjEuY3J0MDkGCCsGAQUFBzABhi1odHRwOi8vb2NzcDIuZ2xvYmFsc2lnbi5jb20vZ3Nk
b21haW52YWxzaGEyZzIwVgYDVR0gBE8wTTBBBgkrBgEEAaAyAQowNDAyBggrBgEFBQcCARYmaHR0
cHM6Ly93d3cuZ2xvYmFsc2lnbi5jb20vcmVwb3NpdG9yeS8wCAYGZ4EMAQIBMAkGA1UdEwQCMAAw
QwYDVR0fBDwwOjA4oDagNIYyaHR0cDovL2NybC5nbG9iYWxzaWduLmNvbS9ncy9nc2RvbWFpbnZh
bHNoYTJnMi5jcmwwHQYDVR0RBBYwFIIJKi5kYXRhLmdsggdkYXRhLmdsMB0GA1UdJQQWMBQGCCsG
AQUFBwMBBggrBgEFBQcDAjAdBgNVHQ4EFgQUX9NXAP7rVkRNBz5pJ558h6fHKBowHwYDVR0jBBgw
FoAU6k581IAt5RWBhiaMgm3AmKTPlw8wggJtBgorBgEEAdZ5AgQCBIICXQSCAlkCVwB1AN3rHSt6
DU+mIIuBrYFocH4ujp0B1VyIjT0RxM227L7MAAABXO5rPTAAAAQDAEYwRAIgHZK+6g7bxKoZGdBR
+fWVVI8vGT7boreySL7bxcpKdg8CIBsaiaZtCNGy/dKaTcQcPURMCSfrjAJGHa2LQ1CZcFrFAHcA
VhQGmi/XwuzT9eG9RLI+x0Z2ubyZEVzA75SYVdaJ0N0AAAFc7ms9MgAABAMASDBGAiEA95X1vk1K
v1VEGqETiQUYGqFCFsqG1gcJflogq0gmOTACIQCwnkoOC86jdlIElCqoJUDys+t+ssfRWQTcXNs8
PA1uSAB1ALvZ37wfinG1k5Qjl6qSe0c4V5UKq1LoGpCWZDaOHtGFAAABXO5rPeQAAAQDAEYwRAIg
KuVIljqwvw4mX83PahEDeOITiVwa7Kc7EEpGW6j0KOwCICAOvbzXKIqTbRNPDS/rq7jyqSIdG6z8
a5TtSsSo3QnsAHYApLkJkLQYWBSHuxOizGdwCjw1mAT5G9+443fNDsgN3BAAAAFc7ms9EgAABAMA
RzBFAiEA7O3L7vhnb3Sg4NEYYDfIinjAxakfDE8jz9H41wxFP0ICIAD8vYev2qxQXVFB9opFULN8
g2nqp/R+UrHa85T6ekSBAHYA7ku9t3XOYLrhQmkfq+GeZqMPfl+wctiDAMR7iXqo/csAAAFc7mtA
IwAABAMARzBFAiAQNgGe3ZTDRS2AUVElHq0p+5/nDhCEO3sA3RWoiyxK7QIhAMKEtt676AsHE1wD
qQqat9zfh792xcSudZLikY9FO6orMA0GCSqGSIb3DQEBCwUAA4IBAQCFFFjIggMGSU21KQmsNgnX
AvRdHBVEPNDqb4yq6zRDz3ESx1bz8y2OHsaGDkvikhrBxYfaOgUqVRicw7jk0Hj9HZgBs30xlhA8
Vx4ryB57bd3JSvBTLRp5354j5a8p9yVkYQyHAe8tslwmYMmuNDrMXPclAKu084uJIjIcDP6sHwSm
eho9bjRL9FCPjLc7S/4rkqKlcdw8NUKd7umluiRdF4V9AjCCHzSoYVbd54sxs9+xeJE8qfBITknP
90OztDdUu/LxpE0ky4/go1TxoFmG5/4+wGS4ybuW3+DhtfsQQmPVbjj1CBVHWPr1KpuaH4hXSvVU
bxy1t+VTAcpvnwZ5"""

# Enable or disable Django admin. Always keep this false in production.
ENABLE_DJANGO_ADMIN = False
