# -*- coding: utf-8 -*-
from django.utils.translation import ugettext as _


class SystemRole(object):
    TYPE_SERVICE = 1
    TYPE_ENTITY = 2
    TYPE_ATTRIBUTE = 3
    TYPE_CUSTOM = 4

    type_choices = (
        (TYPE_SERVICE, _(u"Servicerolle")),
        (TYPE_ENTITY, _(u"Entitetsrolle")),
        (TYPE_ATTRIBUTE, _(u"Attributrolle")),
        (TYPE_CUSTOM, _(u"Tilpasset rolletype")),
    )


class AccessAccount(object):

    STATUS_ACTIVE = 1
    STATUS_BLOCKED = 2
    STATUS_DEACTIVATED = 3

    status_choices = (
        (STATUS_ACTIVE, _(u"Aktiv")),
        (STATUS_BLOCKED, _(u"Blokeret")),
        (STATUS_DEACTIVATED, _(u"Deaktiveret")),
    )


class CertificateUser(object):
    MODE_IDENTIFIES_SINGLE_USER = 1
    MODE_USES_ON_BEHALF_OF = 2

    mode_choices = (
        (MODE_IDENTIFIES_SINGLE_USER, _(u"Identificerer en enkelt bruger")),
        (MODE_USES_ON_BEHALF_OF,
         _(u"Identificerer brugere via 'på-vegne-af'")),
    )

    CERTIFICATE_YEARS_VALID_1 = 1
    CERTIFICATE_YEARS_VALID_3 = 3
    CERTIFICATE_YEARS_VALID_5 = 5

    certificate_years_valid_choices = (
        (CERTIFICATE_YEARS_VALID_1, _(u"1 år")),
        (CERTIFICATE_YEARS_VALID_3, _(u"3 år")),
        (CERTIFICATE_YEARS_VALID_5, _(u"5 år")),
    )


class IdentityProviderAccount(object):

    # If changing any of these values also update DafoMetadataProvider.java in
    # the dafo-sts-saml project.
    USERPROFILE_FORMAT_MULTIVALUE = 1
    USERPROFILE_FORMAT_COMMASEPARATED = 2

    userprofile_attribute_format_choices = (
        (USERPROFILE_FORMAT_MULTIVALUE, _(u"Multiværdi attribut")),
        (USERPROFILE_FORMAT_COMMASEPARATED, _(u"Kommasepareret liste")),
    )

    USERPROFILE_FILTER_NONE = 1
    USERPROFILE_FILTER_REMOVE_PREFIX = 2
    USERPROFILE_FILTER_REMOVE_POSTFIX = 3

    userprofile_filter_choices = (
        (USERPROFILE_FILTER_NONE, _(u"Ingen tilpasninger")),
        (USERPROFILE_FILTER_REMOVE_PREFIX, _(u"Fjern angivet prefix")),
        (USERPROFILE_FILTER_REMOVE_POSTFIX, _(u"Fjern angivet postfix")),
    )

    IDP_TYPE_PRIMARY = 1
    IDP_TYPE_SECONDARY = 2

    # If updating this also update DatabaseQueryManager.java in the
    # dafo-sts-library project.
    IDP_UPDATE_TIMESTAMP_NAME = "LAST_IDP_UPDATE"

    idp_type_choices = (
        (IDP_TYPE_PRIMARY,
         _(u"Primær IdP (ingen validering af brugerprofiler)")),
        (IDP_TYPE_SECONDARY,
         _(u"Sekundær IdP (kan kun udstede angivne brugerprofiler)")),
    )