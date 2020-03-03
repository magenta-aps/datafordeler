# -*- coding: utf-8 -*-
# from django.shortcuts import render

import os
import urllib

from dafousers.auth import update_user_auth_info
from dafousers.model_constants import AccessAccount as AccessAccountConstants
from dafousers.models import IdentityProviderAccount
from django.conf import settings
from django.contrib.auth import authenticate
from django.contrib.auth import login as auth_login
from django.contrib.auth.decorators import login_required
from django.contrib.auth.forms import AuthenticationForm
from django.contrib.auth.views import _get_login_redirect_url
from django.contrib.sites.shortcuts import get_current_site
from django.core.exceptions import PermissionDenied
from django.http import Http404
from django.http import HttpResponseRedirect
from django.template.response import TemplateResponse
from django.urls import reverse
from django.utils.decorators import method_decorator
from django.views.decorators.cache import never_cache
from django.views.decorators.debug import sensitive_post_parameters
from django.views.generic import TemplateView


class IndexView(TemplateView):
    template_name = 'start.html'

    def dispatch(self, *args, **kwargs):
        auth_info = update_user_auth_info(self.request)
        if auth_info and auth_info.system_roles.filter(
                role_name__in=["DAFO Serviceudbyder", "DAFO Administrator"]
        ).exists():
            return HttpResponseRedirect(reverse("common:frontpage"))

        return super(IndexView, self).dispatch(*args, **kwargs)


class ErrorView(TemplateView):
    template_name = 'error.html'


class LoginRequiredMixin(object):
    """Include to require login."""

    needs_userprofiles = None
    needs_userprofiles_any = None
    needs_system_roles = None
    # By default users need to be either an administrator or a service
    # provider to get manage anything in the system.
    needs_system_roles_any = ["DAFO Serviceudbyder", "DAFO Administrator"]

    def check_has_authinfo(self):
        if not self.authinfo:
            raise PermissionDenied(
                "User is not a DAFO user!",
            )

    def check_userprofiles(self):
        # If needs_userprofiles is defined check that the user has all
        # the needed permissions
        if self.needs_userprofiles is not None:
            self.check_has_authinfo()
            match_nr = self.authinfo.user_profiles.filter(
                name__in=self.needs_userprofiles
            ).count()
            if match_nr < len(self.needs_userprofiles):
                raise PermissionDenied(
                    "User does not have all neccessary userprofiles: %s" % (
                        self.needs_userprofiles
                    )
                )
        # If needs_userprofiles_any is defined check that the user has any
        # of the specified_permissions
        if self.needs_userprofiles_any is not None:
            self.check_has_authinfo()
            if not self.authinfo.user_profiles.filter(
                    name__in=self.needs_userprofiles_any
            ).exists():
                raise PermissionDenied(
                    "User does not have any of the needed userprofiles: %s" % (
                        self.needs_userprofiles_any
                    )
                )

    def check_system_roles(self):
        if self.needs_system_roles is not None:
            self.check_has_authinfo()
            match_nr = self.authinfo.system_roles.filter(
                role_name__in=self.needs_system_roles
            ).count()
            if match_nr < len(self.needs_system_roles):
                raise PermissionDenied(
                    "User does not have all neccessary system roles: %s" % (
                        self.needs_system_roles
                    )
                )
        if self.needs_system_roles_any is not None:
            self.check_has_authinfo()
            if not self.authinfo.system_roles.filter(
                    role_name__in=self.needs_system_roles_any
            ).exists():
                raise PermissionDenied(
                    "User does not have any of the needed system roles: %s" % (
                        self.needs_system_roles_any
                    )
                )

    @method_decorator(login_required)
    def dispatch(self, *args, **kwargs):
        """Check for login and dispatch the view."""

        self.authinfo = update_user_auth_info(self.request)
        self.check_userprofiles()
        self.check_system_roles()

        return super(LoginRequiredMixin, self).dispatch(*args, **kwargs)

    # Automatically add user to any form arguments when behind LoginRequired.
    def get_form_kwargs(self):
        kwargs = super(LoginRequiredMixin, self).get_form_kwargs()
        kwargs["user"] = self.request.user
        return kwargs


class LoginView(TemplateView):
    template_name = 'login.html'
    redirect_field_name = 'next'
    authentication_form = AuthenticationForm
    redirect_authenticated_user = True
    process_django_login = False

    @method_decorator(sensitive_post_parameters())
    @method_decorator(never_cache)
    def dispatch_to_django_auth(self, request):
        # This code is copy/pasted from login in django.contrib.auth.views.
        # This is needed to avoid problems with csrf.
        redirect_to = request.POST.get(
            self.redirect_field_name,
            request.GET.get(self.redirect_field_name, '')
        )

        if self.redirect_authenticated_user and request.user.is_authenticated:
            redirect_to = _get_login_redirect_url(request, redirect_to)
            if redirect_to == request.path:
                raise ValueError(
                    "Redirection loop for authenticated user detected. "
                    "Check that your LOGIN_REDIRECT_URL doesn't point to "
                    "a login page."
                )
            return HttpResponseRedirect(redirect_to)
        elif request.method == "POST" and self.process_django_login:
            form = self.authentication_form(request, data=request.POST)
            if form.is_valid():
                auth_login(request, form.get_user())
                # Delete any lingering token from a previous login
                if "token" in request.session:
                    del request.session["token"]
                return HttpResponseRedirect(_get_login_redirect_url(
                    request, redirect_to
                ))
        else:
            form = self.authentication_form(request)

        current_site = get_current_site(request)

        context = {
            'form': form,
            self.redirect_field_name: redirect_to,
            'site': current_site,
            'site_name': current_site.name,
        }

        return TemplateResponse(request, self.template_name, context)

    def get_context_data(self, **kwargs):
        kwargs['idps'] = IdentityProviderAccount.objects.filter(
            status=AccessAccountConstants.STATUS_ACTIVE
        )
        return kwargs

    def get(self, request, *args, **kwargs):
        # Dispatch to Django login to get redirect or context from there
        result = self.dispatch_to_django_auth(request)
        if isinstance(result, TemplateResponse):
            kwargs.update(result.context_data)
            return super(LoginView, self).get(request, *args, **kwargs)
        else:
            return result

    def post(self, request, *args, **kwargs):
        self.process_django_login = True
        # Check for a chosen idp and redirect to SSO proxy service if one is
        # present.
        idp_pk = request.POST.get('idp', None)
        if idp_pk is not None:
            self.process_django_login = False
            try:
                idp = IdentityProviderAccount.objects.get(pk=idp_pk)
                # Redirect to URL proxy service
                return HttpResponseRedirect(
                    settings.IDP_SSOPROXY_URL + '?' +
                    urllib.urlencode((
                        ('dafo_ssoproxy_url', request.build_absolute_uri()),
                        ('dafo_ssoproxy_idp', idp.idp_entity_id),
                        ('dafo_ssoproxy_returnparam', 'token')
                    ))
                )
            except IdentityProviderAccount.DoesNotExist:
                # continue to other login forms
                pass

        token = request.POST.get('token')
        if token is not None:
            self.process_django_login = False
            # Dispatch to authentification backend
            user = authenticate(token=token)
            if user is not None:
                auth_login(request, user)
                request.session['token'] = token
                redirect_to = request.POST.get(
                    self.redirect_field_name,
                    request.GET.get(self.redirect_field_name, '')
                )
                return HttpResponseRedirect(_get_login_redirect_url(
                    request, redirect_to
                ))

        # Dispatch to Django login to get redirect or context from there
        result = self.dispatch_to_django_auth(request)
        if isinstance(result, TemplateResponse):
            kwargs.update(result.context_data)
            return super(LoginView, self).get(request, *args, **kwargs)
        else:
            return result


class FrontpageView(LoginRequiredMixin, TemplateView):
    template_name = 'frontpage.html'


class RstDocView(TemplateView):
    template_name = 'restructured_text_doc.html'

    def get_context_data(self, **kwargs):
        kwargs = super(RstDocView, self).get_context_data(**kwargs)
        doc_file = self.kwargs.get("docfile") + ".rst"
        if doc_file is None:
            raise Http404("Rst doc file %s not found" % doc_file)
        doc_file_path = os.path.join(
            settings.DOC_DIR,
            *doc_file.split("/")
        )
        if not (
                    os.path.exists(doc_file_path) and
                    os.path.isfile(doc_file_path)
        ):
            raise Http404("%s is not an RST file" % doc_file_path)
        with open(doc_file_path) as f:
            kwargs['rst_content'] = f.read()

        return kwargs