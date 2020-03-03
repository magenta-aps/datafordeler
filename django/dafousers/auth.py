# -*- coding: utf-8 -*-
from __future__ import unicode_literals

import datetime
import zlib
from base64 import b64decode
from xml.etree import ElementTree

from dafousers.model_constants import SystemRole as sr_contants
from dafousers.models import AreaRestriction
from dafousers.models import IdentityProviderAccount
from dafousers.models import PasswordUser
from dafousers.models import SystemRole
from dafousers.models import UserProfile
from django.conf import settings
from django.contrib.auth.models import User
from django.core.exceptions import PermissionDenied
from django.db.models import Func, F
from django.utils import timezone
from signxml import XMLVerifier


class DafoUsersAuthBackend(object):

    def authenticate(self, username=None, password=None, token=None):
        if token is not None:
            try:
                verifier = TokenVerifier(token)
                verifier.verify()
            except Exception as e:
                print "Failed to verify token: %s" % e
                raise e
            try:
                # Users from external IdPs will all reference a Django
                # user that matches the UserIdentification that is used for
                # the NameQualifier on the NameID in the token.
                if (verifier.namequalifier is not None and
                        verifier.namequalifier != "<none>"):
                    username = "[%s]" % verifier.namequalifier
                else:
                    username = verifier.get_username()
                djangouser = User.objects.get(
                    username=username,
                    email=verifier.email
                )
            except User.DoesNotExist:
                djangouser = User(
                    username=username,
                    email=verifier.email,
                )
                djangouser.save()

            return djangouser

        try:
            pwuser = PasswordUser.objects.get(email=username)
            if not pwuser.check_password(password):
                return None
        except PasswordUser.DoesNotExist:
            pwuser = None

        if pwuser:
            try:
                djangouser = User.objects.get(username=pwuser.email)
            except User.DoesNotExist:
                # Create a new user. There's no need to set a password
                # because only the password PasswordUser is checked.
                djangouser = User(
                    username=pwuser.email,
                    email=pwuser.email,
                )
                djangouser.save()

            return djangouser

        return None

    def get_user(self, user_id):
        try:
            return User.objects.get(pk=user_id)
        except User.DoesNotExist:
            pass

        return None

    def validate_token(root_elem):
        return True


def decode_and_inflate_token(token):
    try:
        deflated_token = b64decode(token)
        assertion_string = zlib.decompress(
            deflated_token,
            # Negative window arugment tells zlib to skip headers, which
            # is needed to parse correctly formatted tokens.
            -15
        )
        return assertion_string
    except Exception as e:
        raise PermissionDenied(
            "Could not decode and inflate token: %s" % e
        )


def update_user_auth_info(request):

    info = None

    if not hasattr(request, 'user') or not request.user.is_authenticated():
        return info

    if hasattr(request.user, 'dafoauthinfo'):
        return request.user.dafoauthinfo

    # If session contains a DAFO token, go with whatever data is in that
    token = request.session.get("token")
    if token is not None:
        assertion_string = decode_and_inflate_token(token)
        try:
            assertion = ElementTree.fromstring(assertion_string)
        except Exception as e:
            raise PermissionDenied("Could not parse token: %s" % e)

        subject = assertion.find(
            "{urn:oasis:names:tc:SAML:2.0:assertion}"
            "Subject"
        )
        nameID = subject.find(
            "{urn:oasis:names:tc:SAML:2.0:assertion}"
            "NameID"
        )
        namequalifier = nameID.get("NameQualifier")
        if namequalifier == "<none>":
            # Token is provided by the primary IdP, look up a PasswordUser
            # by matching nameID of token to email
            user = PasswordUser.objects.filter(
                email=nameID.text
            ).first()
            info = DafoAuthInfo(user)
        else:
            # Otherwise, lookup the IdentityProviderAccount by matching
            # the namequalifier against user_id of UserIdentification
            user = IdentityProviderAccount.objects.filter(
                identified_user__user_id=namequalifier
            ).first()
            primary_type = IdentityProviderAccount.CONSTANTS.IDP_TYPE_PRIMARY
            if user.idp_type == primary_type:
                # Use all associated user profiles for this user
                info = DafoAuthInfo(user)
            else:
                # Use only user profiles stored in token
                result = []
                attr_map = settings.USERPROFILE_DEBUG_TRANSLATION_MAP
                try:
                    attr_statement = assertion.find(
                        "{urn:oasis:names:tc:SAML:2.0:assertion}"
                        "AttributeStatement"
                    )
                    for attribute in attr_statement.findall(
                        "{urn:oasis:names:tc:SAML:2.0:assertion}"
                        "Attribute"
                    ):
                        attr_name = attribute.get("Name", "")
                        if attr_name == "https://data.gl/claims/userprofile":
                            attr_value = attribute.find(
                                "{urn:oasis:names:tc:SAML:2.0:assertion}"
                                "AttributeValue"
                            )
                            value = unicode(attr_value.text)
                            translated = attr_map.get(value)
                            if translated is not None:
                                for x in translated:
                                    result.append(x)
                            else:
                                result.append(value)
                except Exception as e:
                    raise PermissionDenied(
                        "Could not get userprofiles from token: %s" % e
                    )
                info = DafoAuthInfo(user, UserProfile.objects.filter(
                    name__in=result
                ))

        if info is None:
            raise PermissionDenied(
                "No user or organisation found matching %s or %s" % (
                    nameID.text, namequalifier
                )
            )
    else:
        # Try looking up a passworduser
        try:
            pwuser = PasswordUser.objects.get(email=request.user.email)
            info = DafoAuthInfo(pwuser)
        except PasswordUser.DoesNotExist:
            info = None

    request.user.dafoauthinfo = info

    return info


class DafoAuthInfo(object):
    user_profiles_qs = None
    system_roles_qs = None

    def __init__(self, user, profiles_queryset=None):
        self.access_account_user = user
        if profiles_queryset is None:
            profiles_queryset = user.user_profiles.all()
        self.user_profiles_qs = profiles_queryset
        self.system_roles_qs = SystemRole.objects.filter(
            userprofile__in=self.user_profiles
        )

    def has_user_profile(self, name):
        return self.user_profiles_qs.filter(
            name=name
        ).exists()

    def has_system_role(self, name):
        return self.system_roles_qs.filter(
            name=name
        ).exists()

    @property
    def user_profiles(self):
        return self.user_profiles_qs.all()

    @property
    def system_roles(self):
        return self.system_roles_qs.all()

    @property
    def admin_user_profiles(self):
        if self.has_user_profile("DAFO Administrator"):
            return UserProfile.objects.all()
        elif self.has_user_profile("DAFO Serviceudbyder"):
            return self.user_profiles_qs.exclude(
                name__in=[
                    "DAFO Administrator",
                    "DAFO Serviceudbyder",
                ]
            )
        else:
            return UserProfile.objects.none()

    @property
    def admin_system_roles(self):
        if self.has_user_profile("DAFO Administrator"):
            return SystemRole.objects.all()
        elif self.has_user_profile("DAFO Serviceudbyder"):
            return SystemRole.objects.filter(
                userprofile__in=self.admin_user_profiles
            ).distinct()
        else:
            return SystemRole.objects.none()

    @property
    def admin_area_restrictions(self):
        if self.has_user_profile("DAFO Administrator"):
            return AreaRestriction.objects.all()
        elif self.has_user_profile("DAFO Serviceudbyder"):
            # A serviceprovider's area restrictions are limited to restrictions
            # for services he has access to.
            return AreaRestriction.objects.annotate(
                service_name_lower=Func(
                    F('area_restriction_type__service_name'),
                    function='LOWER'
                )
            ).filter(
                service_name_lower__in=[
                    x.target_name.lower() for x in
                    self.admin_system_roles.filter(
                        role_type=sr_contants.TYPE_SERVICE
                    )
                ]
            ).distinct()
        else:
            return AreaRestriction.objects.none()


class TokenVerifier(object):

    def __init__(self, incoming_token):
        self.email = None
        self.namequalifier = None
        self.assertion_string = decode_and_inflate_token(incoming_token)
        try:
            self.assertion = ElementTree.fromstring(self.assertion_string)
        except Exception as e:
            raise PermissionDenied("Could not parse token: %s" % e)

    def verify(self):
        self.verify_token_age()
        self.verify_issuer()
        self.verify_subject()
        self.verify_conditions()
        self.verify_signature_and_trust()

        return self.assertion

    def get_username(self):
        return "[%s]" % "]@[".join([
            self.email if self.email is not None else "<none>",
            self.namequalifier if self.namequalifier is not None else "<none>"
        ])

    def parse_datetime(self, text):
        unawere = timezone.datetime.strptime(
            text, "%Y-%m-%dT%H:%M:%S.%fZ"
        )
        return timezone.make_aware(unawere, timezone=timezone.UTC())

    def check_time_skew(self, time_string, forward_interval_sec):
        check_against = self.parse_datetime(time_string)

        reference = timezone.now()
        diff = reference - check_against
        if diff < datetime.timedelta(seconds=-settings.MAX_SAML_TIME_SKEW):
            return False
        if diff > datetime.timedelta(
            seconds=forward_interval_sec + settings.MAX_SAML_TIME_SKEW
        ):
            return False
        return True

    def check_not_before(self, check_datetime):
        check_datetime = self.parse_datetime(check_datetime)
        diff = timezone.now() - check_datetime
        return diff >= datetime.timedelta(seconds=-settings.MAX_SAML_TIME_SKEW)

    def check_not_on_or_after(self, check_datetime):
        check_datetime = self.parse_datetime(check_datetime)
        diff = timezone.now() - check_datetime
        return diff < datetime.timedelta(seconds=settings.MAX_SAML_TIME_SKEW)

    def get_idp(self):
        if self._idp is None:
            try:
                self._idp = IdentityProviderAccount.objects.get(
                    issuer.text
                )
            except Exception as e:
                raise PermissionDenied(
                    "Issuer does not match a registered IdP"
                )
        return self._idp

    def verify_token_age(self):
        issue_instant = self.assertion.get("IssueInstant")
        if not self.check_time_skew(
            issue_instant, settings.MAX_SAML_TOKEN_LIFETIME
        ):
            raise PermissionDenied("Token is too old")

    def verify_issuer(self):
        issuer = self.assertion.find(
            "{urn:oasis:names:tc:SAML:2.0:assertion}"
            "Issuer"
        )
        if issuer is None:
            raise PermissionDenied("No Issuer in token")
        if issuer.text != settings.SAML_STS_ISSUER:
            raise PermissionDenied("Wrong Issuer in token")

    def verify_subject(self):
        subject = self.assertion.find(
            "{urn:oasis:names:tc:SAML:2.0:assertion}"
            "Subject"
        )
        if subject is None:
            raise PermissionDenied("No Subject defined in token")
        nameID = subject.find(
            "{urn:oasis:names:tc:SAML:2.0:assertion}"
            "NameID"
        )
        if nameID is None:
            raise PermissionDenied("No NameID defined in token")
        self.email = nameID.text
        self.namequalifier = nameID.get("NameQualifier")
        try:
            subjectconfdata = subject.find(
                "{urn:oasis:names:tc:SAML:2.0:assertion}"
                "SubjectConfirmation"
            ).find(
                "{urn:oasis:names:tc:SAML:2.0:assertion}"
                "SubjectConfirmationData"
            )
        except:
            raise PermissionDenied(
                "No SubjectConfirmationData defined in token"
            )
        not_on_or_after = subjectconfdata.get("NotOnOrAfter")
        if not_on_or_after is None:
            raise PermissionDenied(
                "No NotOnOrAfter for SubjectConfirmationData"
            )
        if not self.check_not_on_or_after(not_on_or_after):
            raise PermissionDenied(
                "Failed NotOnOrAfter for SubjectConfirmationData"
            )

    def verify_conditions(self):
        conditions = self.assertion.find(
            "{urn:oasis:names:tc:SAML:2.0:assertion}"
            "Conditions"
        )
        if conditions is None:
            raise PermissionDenied("No Conditions in token")

        not_before = conditions.get("NotBefore")
        if not_before is None:
            raise PermissionDenied("No NotBefore on Conditions in token")
        if not self.check_not_before(not_before):
            raise PermissionDenied(
                "Failed NotBefore for Conditions"
            )
        not_on_or_after = conditions.get("NotOnOrAfter")

        if not_on_or_after is None:
            raise PermissionDenied(
                "No NotOnOrAfter for Conditions"
            )
        if not self.check_not_on_or_after(not_on_or_after):
            raise PermissionDenied(
                "Failed NotOnOrAfter for Conditions"
            )

        audience_restriction = conditions.find(
            "{urn:oasis:names:tc:SAML:2.0:assertion}"
            "AudienceRestriction"
        )
        if audience_restriction is None:
            raise PermissionDenied(
                "No AudienceRestriction in Conditions"
            )
        audience = audience_restriction.find(
            "{urn:oasis:names:tc:SAML:2.0:assertion}"
            "Audience"
        )

        if audience is None:
            raise PermissionDenied("No Audience in Conditions")
        if audience.text != settings.SAML_AUDIENCE_URI:
            raise PermissionDenied(
                "Audience URL %s does not match %s" % (
                    audience.text, settings.SAML_AUDIENCE_URI
                )
            )

    def verify_signature_and_trust(self):
        signature = self.assertion.find(
            "{http://www.w3.org/2000/09/xmldsig#}"
            "Signature"
        )
        if signature is None:
            raise PermissionDenied("No Signature in token")
        try:
            token_cert = signature.find(
                "{http://www.w3.org/2000/09/xmldsig#}"
                "KeyInfo"
            ).find(
                "{http://www.w3.org/2000/09/xmldsig#}"
                "X509Data"
            ).find(
                "{http://www.w3.org/2000/09/xmldsig#}"
                "X509Certificate"
            ).text
        except:
            token_cert = None
        if token_cert is None:
            raise PermissionDenied("No X509Certificate in token")
        if token_cert != settings.SAML_STS_PUBLIC_CERT:
            raise PermissionDenied("Token signed with untrusted certificate")
        try:
            signed_data = XMLVerifier().verify(
                self.assertion_string,
                x509_cert=settings.SAML_STS_PUBLIC_CERT
            ).signed_data
        except:
            signed_data = None
        if signed_data is None:
            raise PermissionDenied("Invalid signature for token")
