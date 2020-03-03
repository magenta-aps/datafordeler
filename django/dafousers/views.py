# -*- coding: utf-8 -*-
# from django.shortcuts import render

import re
import tempfile

from common.views import LoginRequiredMixin
from dafousers import models, forms
from django.contrib.messages.views import SuccessMessageMixin
from django.db.models import Min
from django.http import HttpResponse
from django.shortcuts import render
from django.urls import reverse
from django.views.generic import View, UpdateView
from django.views.generic.edit import CreateView
from django.views.generic.list import ListView


class AccessAccountUserAjaxUpdate(LoginRequiredMixin, View):
    model = None

    def post(self, request, *args, **kwargs):
        authinfo = self.request.user.dafoauthinfo
        ids = request.POST.getlist('user_id')
        users = self.model.objects.filter(id__in=ids)
        action = request.POST.get('action')

        if '_status' in action:
            parts = action.split("_")
            status = int(parts[2])
            for x in users:
                if x.status != status:
                    x.status = status
                    # Save user to trigger history update
                    x.changed_by = self.request.user.username
                    x.save()
            return HttpResponse('Statusser er blevet opdateret.')
        elif action == '_add_user_profiles':
            user_profiles_ids = request.POST.getlist('user_profiles')
            user_profiles = authinfo.admin_user_profiles.filter(
                id__in=user_profiles_ids
            )
            for user in users:
                changed = False
                existing = set(user.user_profiles.all())
                for x in user_profiles:
                    if x not in existing:
                        user.user_profiles.add(x)
                        changed = True
                if changed:
                    # Save user to trigger history update
                    user.changed_by = self.request.user.username
                    user.save()

        return HttpResponse('Tildelte brugerprofiler er blevet opdateret.')


class AccessAccountListTable(LoginRequiredMixin, ListView):
    template_name = None
    get_queryset_method = None

    def get_queryset(self):
        method = type(self).__dict__["get_queryset_method"]
        return method(
            self.request.GET.get('filter', None),
            self.request.GET.get('order', None)
        )


class PasswordUserCreate(LoginRequiredMixin, SuccessMessageMixin, CreateView):
    template_name = 'dafousers/passworduser/add.html'
    form_class = forms.PasswordUserForm
    model = models.PasswordUser

    def form_valid(self, form):
        form.instance.changed_by = self.request.user.username

        userId = models.UserIdentification(user_id=form.instance.email)
        userId.save()
        form.instance.identified_user = userId
        salt, encrypted_password = \
            form.instance.generate_encrypted_password_and_salt(
                form.cleaned_data['password']
            )
        form.instance.password_salt = salt
        form.instance.encrypted_password = encrypted_password

        result = super(PasswordUserCreate, self).form_valid(form)
        form.instance.user_profiles = form.cleaned_data['user_profiles']
        form.instance.save()
        return result

    def get_success_url(self):
        action = self.request.POST.get('action')
        if action == '_save':
            return reverse('dafousers:passworduser-list')
        elif action == '_addanother':
            return reverse('dafousers:passworduser-add')

    def get_success_message(self, cleaned_data):
        return 'Brugeren {} er blevet oprettet.'.format(self.object)


class PasswordUserList(LoginRequiredMixin, ListView):
    template_name = 'dafousers/passworduser/list.html'
    model = models.PasswordUser

    def get_context_data(self, **kwargs):
        context = super(PasswordUserList, self).get_context_data(**kwargs)
        context['action'] = ""
        context['user_profiles'] = models.UserProfile.objects.all()
        context['filter'] = ''
        context['order'] = 'name'
        context['object_list'] = get_passworduser_queryset(context['filter'], context['order'])
        return context


class PasswordUserHistory(LoginRequiredMixin, ListView):
    template_name = 'dafousers/passworduser/history.html'
    model = models.PasswordUserHistory

    def get_context_data(self, **kwargs):
        context = super(PasswordUserHistory, self).get_context_data(**kwargs)
        pk = self.kwargs['pk']
        context['password_user_id'] = pk
        context['history'] = models.PasswordUserHistory.objects.filter(
            entity=models.PasswordUser.objects.get(pk=pk)
        ).order_by("-updated")
        return context


class PasswordUserEdit(LoginRequiredMixin, SuccessMessageMixin, UpdateView):
    template_name = 'dafousers/passworduser/edit.html'
    model = models.PasswordUser
    form_class = forms.PasswordUserForm
    success_url = 'user/list/'

    def form_valid(self, form):
        form.instance.changed_by = self.request.user.username
        if form.cleaned_data['password'] != "*":
            salt, encrypted_password = form.instance.generate_encrypted_password_and_salt(form.cleaned_data['password'])
            form.instance.password_salt = salt
            form.instance.encrypted_password = encrypted_password

        return super(PasswordUserEdit, self).form_valid(form)

    def get_success_url(self):
        action = self.request.POST.get('action')
        if action == '_save':
            return reverse('dafousers:passworduser-list')

    def get_success_message(self, cleaned_data):
        return 'Brugeren {} er blevet opdateret.'.format(self.object)


def get_passworduser_queryset(filter, order):
    # If a filter param is passed, we use it to filter
    if filter:
        password_users = models.PasswordUser.objects.filter(status=filter)
    else:
        password_users = models.PasswordUser.objects.all()

    # If a order param is passed, we use it to order
    if order:
        if order == "name":
            password_users = password_users.order_by("givenname", "lastname")
        elif order == "-name":
            password_users = password_users.order_by("-givenname", "-lastname")
        else:
            password_users = password_users.order_by(order)

    return password_users


class PasswordUserAjaxUpdate(AccessAccountUserAjaxUpdate):
    model = models.PasswordUser


class PasswordUserListTable(AccessAccountListTable):
    get_queryset_method = get_passworduser_queryset
    template_name = 'dafousers/passworduser/table.html'


class CertificateUserCreate(LoginRequiredMixin, SuccessMessageMixin, CreateView):
    template_name = 'dafousers/certificateuser/add.html'
    form_class = forms.CertificateUserForm
    model = models.CertificateUser

    def form_valid(self, form):
        form.instance.changed_by = self.request.user.username

        user_id = models.UserIdentification(user_id=form.instance.contact_email)
        user_id.save()
        form.instance.identified_user = user_id

        result = super(CertificateUserCreate, self).form_valid(form)
        form.instance.user_profiles = form.cleaned_data['user_profiles']
        certificate_years_valid = form.cleaned_data['certificate_years_valid']
        form.instance.create_certificate(int(certificate_years_valid))
        form.instance.save()
        return result

    def get_success_url(self):
        action = self.request.POST.get('action')
        if action == '_save':
            return reverse('dafousers:certificateuser-list')
        elif action == '_addanother':
            return reverse('dafousers:certificateuser-add')

    def get_success_message(self, cleaned_data):
        return 'Systemet {} er blevet oprettet.'.format(self.object)


class CertificateUserList(LoginRequiredMixin, ListView):
    template_name = 'dafousers/certificateuser/list.html'
    model = models.CertificateUser

    def get_context_data(self, **kwargs):
        context = super(CertificateUserList, self).get_context_data(**kwargs)
        context['action'] = ""
        context['user_profiles'] = models.UserProfile.objects.all()
        context['filter'] = ''
        context['order'] = 'name'
        context['object_list'] = get_certificateuser_queryset(context['filter'], context['order'])
        return context


class CertificateUserHistory(LoginRequiredMixin, ListView):
    template_name = 'dafousers/certificateuser/history.html'
    model = models.CertificateUserHistory

    def get_context_data(self, **kwargs):
        context = super(CertificateUserHistory, self).get_context_data(**kwargs)
        pk = self.kwargs['pk']
        context['certificate_user_id'] = pk
        context['history'] = models.CertificateUserHistory.objects.filter(
            entity=models.CertificateUser.objects.get(pk=pk)
        ).order_by("-updated")
        return context


class CertificateUserEdit(LoginRequiredMixin, SuccessMessageMixin, UpdateView):
    template_name = 'dafousers/certificateuser/edit.html'
    model = models.CertificateUser
    form_class = forms.CertificateUserForm
    success_url = '/system/list/'
    create_new_certificate = False

    def form_valid(self, form):
        form.instance.changed_by = self.request.user.username
        form.instance.certificates = form.cleaned_data['certificates']

        # Do we want to remove certs from the database?
        for cert in form.instance.certificates.all():
            removed_cert = form.cleaned_data['certificates'].filter(pk=cert.pk)
            if not removed_cert:
                form.instance.certificates.remove(cert.pk)

        if self.request.POST.get('create_new_certificate'):
            certificate_years_valid = form.cleaned_data['certificate_years_valid']
            print certificate_years_valid
            form.instance.create_certificate(int(certificate_years_valid))
        return super(CertificateUserEdit, self).form_valid(form)

    def get_success_url(self):
        action = self.request.POST.get('action')
        if action == '_save':
            return reverse('dafousers:certificateuser-list')

    def get_success_message(self, cleaned_data):
        return 'Systemet {} er blevet opdateret.'.format(self.object)


def get_certificateuser_queryset(filter, order):
    # If a filter param is passed, we use it to filter
    if filter:
        certificate_users = models.CertificateUser.objects.filter(status=filter)
    else:
        certificate_users = models.CertificateUser.objects.all()

    # If a order param is passed, we use it to order
    if order:
        if "next_expiration_subject" in order:
            certificate_users = certificate_users.annotate(
                next_expiration_subject=Min('certificates__subject')
            )
        elif "next_expiration" in order:
            certificate_users = certificate_users.annotate(
                next_expiration=Min('certificates__valid_to')
            )
        certificate_users = certificate_users.order_by(order)
    return certificate_users


class CertificateUserAjaxUpdate(AccessAccountUserAjaxUpdate):
    model = models.CertificateUser


class CertificateUserListTable(AccessAccountListTable):
    template_name = 'dafousers/certificateuser/table.html'
    get_queryset_method = get_certificateuser_queryset


class IdentityProviderAccountCreate(LoginRequiredMixin, SuccessMessageMixin, CreateView):
    template_name = 'dafousers/identityprovideraccount/add.html'
    model = models.IdentityProviderAccount

    def get_form_class(self):
        if self.request.user.dafoauthinfo.has_user_profile(
            "DAFO Administrator"
        ):
            return forms.IdentityProviderAccountForm
        else:
            return forms.IdentityProviderAccountFormRestricted

    def form_valid(self, form):
        form.instance.changed_by = self.request.user.username

        user_id = models.UserIdentification(user_id=form.instance.contact_email)
        user_id.save()
        form.instance.identified_user = user_id

        result = super(IdentityProviderAccountCreate, self).form_valid(form)

        form.instance.user_profiles = form.cleaned_data['user_profiles']

        form.instance.save_metadata()
        form.instance.save()

        return result

    def get_success_url(self):
        action = self.request.POST.get('action')
        if action == '_save':
            return reverse('dafousers:identityprovideraccount-list')
        elif action == '_addanother':
            return reverse('dafousers:identityprovideraccount-add')

    def get_success_message(self, cleaned_data):
        return 'Organisationen {} er blevet oprettet.'.format(self.object)


class IdentityProviderAccountList(LoginRequiredMixin, ListView):
    template_name = 'dafousers/identityprovideraccount/list.html'
    model = models.IdentityProviderAccount

    def get_context_data(self, **kwargs):
        context = super(IdentityProviderAccountList, self).get_context_data(**kwargs)
        context['action'] = ""
        context['user_profiles'] = models.UserProfile.objects.all()
        context['filter'] = ''
        context['order'] = 'name'
        context['object_list'] = get_identityprovideraccount_queryset(context['filter'], context['order'])
        return context


class IdentityProviderAccountHistory(LoginRequiredMixin, ListView):
    template_name = 'dafousers/identityprovideraccount/history.html'
    model = models.IdentityProviderAccountHistory

    def get_context_data(self, **kwargs):
        context = super(IdentityProviderAccountHistory, self).get_context_data(**kwargs)
        pk = self.kwargs['pk']
        context['identityprovider_account_id'] = pk
        context['history'] = models.IdentityProviderAccountHistory.objects.filter(
            entity=models.IdentityProviderAccount.objects.get(pk=pk)
        ).order_by("-updated")
        return context


class IdentityProviderAccountEdit(LoginRequiredMixin, SuccessMessageMixin, UpdateView):
    template_name = 'dafousers/identityprovideraccount/edit.html'
    model = models.IdentityProviderAccount
    success_url = '/organisation/list/'

    def get_form_class(self):
        if self.request.user.dafoauthinfo.has_user_profile(
            "DAFO Administrator"
        ):
            return forms.IdentityProviderAccountForm
        else:
            return forms.IdentityProviderAccountFormRestricted

    def form_valid(self, form):
        form.instance.changed_by = self.request.user.username
        form.instance.save_metadata()

        return super(IdentityProviderAccountEdit, self).form_valid(form)

    def get_success_url(self):
        action = self.request.POST.get('action')
        if action == '_save':
            return reverse('dafousers:identityprovideraccount-list')

    def get_success_message(self, cleaned_data):
        return 'Organisationen {} er blevet opdateret.'.format(self.object)


def get_identityprovideraccount_queryset(filter, order):
    # If a filter param is passed, we use it to filter
    if filter:
        identityprovider_accounts = models.IdentityProviderAccount.objects.filter(status=filter)
    else:
        identityprovider_accounts = models.IdentityProviderAccount.objects.all()

    # If a order param is passed, we use it to order
    if order:
        identityprovider_accounts = identityprovider_accounts.order_by(order)
    return identityprovider_accounts


class IdentityProviderAccountAjaxUpdate(AccessAccountUserAjaxUpdate):
    model = models.IdentityProviderAccount


class IdentityProviderAccountListTable(AccessAccountListTable):
    get_queryset_method = get_identityprovideraccount_queryset
    template_name = 'dafousers/identityprovideraccount/table.html'


class UserProfileCreate(LoginRequiredMixin, SuccessMessageMixin, CreateView):
    template_name = 'dafousers/userprofile/add.html'
    form_class = forms.UserProfileForm
    model = models.UserProfile

    def form_valid(self, form):
        form.instance.changed_by = self.request.user.username

        result = super(UserProfileCreate, self).form_valid(form)
        form.instance.system_roles = form.cleaned_data['system_roles']
        form.instance.area_restrictions = form.cleaned_data['area_restrictions']
        form.instance.accessaccount_set.add(
            self.request.user.dafoauthinfo.access_account_user
        )
        form.instance.save()
        return result

    def get_success_url(self):
        action = self.request.POST.get('action')
        if action == '_save':
            return reverse('dafousers:userprofile-list')
        elif action == '_addanother':
            return reverse('dafousers:userprofile-add')

    def get_success_message(self, cleaned_data):
        return 'Brugerprofilen {} er blevet oprettet.'.format(self.object)


class UserProfileList(LoginRequiredMixin, ListView):
    template_name = 'dafousers/userprofile/list.html'
    model = models.UserProfile

    def get_context_data(self, **kwargs):
        authinfo = self.request.user.dafoauthinfo
        context = super(UserProfileList, self).get_context_data(**kwargs)
        context['action'] = ""
        context['system_roles'] = authinfo.admin_system_roles
        context['area_restrictions'] = authinfo.admin_area_restrictions
        context['order'] = 'name'
        context['object_list'] = get_userprofile_queryset(
            authinfo.admin_user_profiles, context['order']
        )
        return context


class UserProfileHistory(LoginRequiredMixin, ListView):
    template_name = 'dafousers/userprofile/history.html'
    model = models.UserProfileHistory

    def get_context_data(self, **kwargs):
        context = super(UserProfileHistory, self).get_context_data(**kwargs)
        pk = self.kwargs['pk']
        context['user_profile_id'] = pk
        context['history'] = models.UserProfileHistory.objects.filter(
            entity=models.UserProfile.objects.get(pk=pk)
        ).order_by("-updated")
        return context


class UserProfileEdit(LoginRequiredMixin, SuccessMessageMixin, UpdateView):
    template_name = 'dafousers/userprofile/edit.html'
    model = models.UserProfile
    form_class = forms.UserProfileForm

    def form_valid(self, form):
        form.instance.changed_by = self.request.user.username
        return super(UserProfileEdit, self).form_valid(form)

    def get_success_url(self):
        return reverse('dafousers:userprofile-list')

    def get_success_message(self, cleaned_data):
        return 'Brugerprofilen {} er blevet opdateret.'.format(self.object)


class UserProfileAjaxUpdate(LoginRequiredMixin, View):
    def post(self, request, *args, **kwargs):
        ids = request.POST.getlist('user_id')
        authinfo = request.user.dafoauthinfo
        user_profiles = authinfo.admin_user_profiles.filter(
            id__in=ids
        )
        action = request.POST.get('action')
        type = ''
        if '_status' in action:
            parts = action.split("_")
            status = parts[2]
            user_profiles.update(status=status)
            return HttpResponse('Statusser er blevet opdateret.')
        elif action == '_add_system_roles':
            type = 'systemroller'
            system_roles_ids = request.POST.getlist('system_roles')
            system_roles = authinfo.admin_system_roles.filter(
                id__in=system_roles_ids
            )
            for system_role in system_roles:
                for user_profile in user_profiles:
                    user_profile.system_roles.add(system_role)
        elif action == '_add_area_restrictions':
            type = 'områdeafgrænsninger'
            area_restrictions_ids = request.POST.getlist('area_restrictions')
            area_restrictions = authinfo.admin_area_restrictions.filter(
                id__in=area_restrictions_ids
            )
            for area_restriction in area_restrictions:
                for user_profile in user_profiles:
                    user_profile.area_restrictions.add(area_restriction)
        return HttpResponse('Tildelte %s er blevet opdateret.' % type)


class UserProfileListTable(LoginRequiredMixin, ListView):
    template_name = 'dafousers/userprofile/table.html'

    def get_queryset(self):
        return get_userprofile_queryset(
            self.request.user.dafoauthinfo.admin_user_profiles,
            self.request.GET.get('order', None)
        )


def get_userprofile_queryset(qs, order):
    # If a filter param is passed, we use it to filter

    # If a order param is passed, we use it to order
    if order:
        qs = qs.order_by(order)
    return qs


def search_org_user_system(request):
    search_term = request.GET.get('search_term', None)
    result = []
    if search_term == "":
        return render(request, 'search-autocomplete.html', {'object_list': result})

    users = models.PasswordUser.objects.search(search_term)
    organisations = models.IdentityProviderAccount.objects.search(search_term)
    systems = models.CertificateUser.objects.search(search_term)

    for user in users:
        result.append({
            "text": "Bruger: " + user.givenname + " " + user.lastname,
            "url": reverse('dafousers:passworduser-edit', kwargs={"pk": user.id})
        })

    for organisation in organisations:
        result.append({
            "text": "Organisation: " + organisation.name,
            "url": reverse('dafousers:identityprovideraccount-edit', kwargs={"pk": organisation.id})
        })

    for system in systems:
        result.append({
            "text": "System: " + system.name,
            "url": reverse('dafousers:certificateuser-edit', kwargs={"pk": system.id})
        })

    return render(request, 'search-autocomplete.html', {'object_list': result})


def search_user_profile(request):
    search_term = request.GET.get('search_term', None)
    result = []
    if search_term == "":
        return render(request, 'search-autocomplete.html', {'object_list': result})

    user_profiles = models.UserProfile.objects.search(search_term)

    for user_profile in user_profiles:
        result.append({
            "text": "Brugerprofil: " + user_profile.name,
            "url": reverse('dafousers:userprofile-edit', kwargs={"pk": user_profile.id})
        })

    return render(request, 'search-autocomplete.html', {'object_list': result})


class CertificateDownload(LoginRequiredMixin, View):
    def get(self, request, *args, **kwargs):
        cert = models.Certificate.objects.get(pk=kwargs.get('pk'))
        user = cert.certificateuser_set.last()
        if user is None:
            user = "cert"
        else:
            user = re.sub(r'[^\x00-\x7f]', '_', user.contact_email)
        name_parts = [user]
        if cert.valid_to:
            name_parts.append(cert.valid_to.strftime("%Y-%m-%d_%H-%M-%S"))

        name = "_".join(name_parts)

        resp = HttpResponse(
            cert.certificate_blob,
            content_type='application/x-pkcs12'
        )
        resp['Content-Disposition'] = (
            "attachment; filename=%s.p12" % name
        )
        return resp


class DatabaseCheckView(View):
    def get(self, request, *args, **kwargs):
        count = models.PasswordUser.objects.count()
        return HttpResponse()
