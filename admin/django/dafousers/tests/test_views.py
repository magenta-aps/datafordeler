import logging
from django.urls import reverse
from django.test import TestCase, Client
from dafousers.models import PasswordUser, UserProfile, IdentityProviderAccount, CertificateUser
from dafousers.model_constants import IdentityProviderAccount as IdentityProviderAccountConstants,\
    CertificateUser as CertificateUserConstants


class LoggedIn(TestCase):

    @classmethod
    def setUpClass(cls):
        logging.disable(logging.CRITICAL)

    def setUp(self):
        self.client = Client()
        self.client.login(**{'username': 'jakob@data.nanoq.gl', 'password': 'jacob'})

    @classmethod
    def tearDownClass(cls):
        logging.disable(logging.NOTSET)


class PasswordUserTestCase(LoggedIn):

    def setUp(self):
        super(PasswordUserTestCase, self).setUp()
        self.user = PasswordUser.objects.create(givenname='test_object', lastname='something', email='test@object.com', changed_by='system')
        self.user2 = PasswordUser.objects.create(givenname='test_object2', lastname='something', email='test2@object.com', changed_by='system')

    def test_user_edit(self):
        resp = self.client.post(reverse('dafousers:passworduser-edit', args=[self.user.id]), {'givenname': 'firstname',
                                                                                         'lastname': 'lastname',
                                                                                         'email': 'email@test.com',
                                                                                         'password': '*',  # silly this is required when you are not going to set a password,
                                                                                         'action': '_save',
                                                                                         'status': '1'}, follow=True)
        self.assertRedirects(resp, reverse('dafousers:passworduser-list'))

    def test_user_history(self):
        self.user.email = 'new_email'
        self.user.save()
        resp = self.client.get(reverse('dafousers:passworduser-history', args=[self.user.id]))
        self.assertEqual(resp.status_code, 200)
        self.assertEqual(len(resp.context['history']), 1)

    def test_user_create_action_save(self):
        resp = self.client.post(reverse('dafousers:passworduser-add'), {'givenname': 'firstname', 'lastname': 'lastname',
                                                                        'email': 'email@test.com', 'password': 'test',
                                                                        'status': '1', 'action': '_save'}, follow=True)

        self.assertRedirects(resp, reverse('dafousers:passworduser-list'))
        self.assertTrue(PasswordUser.objects.filter(email='email@test.com').exists())

    def test_user_create_action_add_another(self):
        resp = self.client.post(reverse('dafousers:passworduser-add'), {'givenname': 'firstname_also', 'lastname': 'lastname_also',
                                                                        'email': 'email1@test.com', 'password': 'test2',
                                                                        'status': '2', 'action': '_addanother'}, follow=True)

        self.assertRedirects(resp, reverse('dafousers:passworduser-add'))
        self.assertTrue(PasswordUser.objects.filter(email='email1@test.com').exists())

    def test_user_list(self):
        resp = self.client.get(reverse('dafousers:passworduser-list'))
        # currently jakob and Amalie exists and test_object
        self.assertEqual(resp.status_code, 200)
        self.assertEqual(4, resp.context['object_list'].count())

    def test_ajax_update_password_user_queryset(self):
        resp = self.client.get(reverse('dafousers:update_passworduser_queryset'))
        self.assertEqual(resp.status_code, 200)
        self.assertEqual(resp.context['object_list'].count(), 4)

    def test_ajax_update_password_user_change_status(self):
        resp = self.client.post(reverse('dafousers:update_passworduser'), {'action': 'ad_status_3',
                                                                           'user_id': [self.user.id, self.user2.id]})
        self.assertEqual(resp.status_code, 200)
        # both users should have been updated now
        user = PasswordUser.objects.get(id=self.user.id)
        self.assertEqual(user.status, 3)
        self.assertEqual(user.changed_by, 'jakob@data.nanoq.gl')
        user2 = PasswordUser.objects.get(id=self.user2.id)
        self.assertEqual(user2.status, 3)
        self.assertEqual(user2.changed_by, 'jakob@data.nanoq.gl')

    def test_ajax_update_password_user_add_user_profile(self):
        profile = UserProfile.objects.create(name='test_profile', changed_by='system')
        profile2 = UserProfile.objects.create(name='test_profile2', changed_by='system')

        resp = self.client.post(reverse('dafousers:update_passworduser'), {'action': '_add_user_profiles',
                                                                           'user_id': [self.user.id, self.user2.id],
                                                                           'user_profiles': [profile.id, profile2.id]})
        self.assertEqual(resp.status_code, 200)
        # both users should have been updated now
        user = PasswordUser.objects.get(id=self.user.id)
        self.assertListEqual(list(user.user_profiles.all()), [profile, profile2])
        self.assertEqual(user.changed_by, 'jakob@data.nanoq.gl')
        user2 = PasswordUser.objects.get(id=self.user2.id)
        self.assertListEqual(list(user2.user_profiles.all()), [profile, profile2])
        self.assertEqual(user2.changed_by, 'jakob@data.nanoq.gl')


class SystemTestCase(LoggedIn):

    def setUp(self):
        super(SystemTestCase, self).setUp()
        self.cert_values = {'name': 'new_test_certificate',
                            'identification_mode': CertificateUserConstants.MODE_IDENTIFIES_SINGLE_USER,
                            'status': 1,
                            'contact_email': 'test@test.dk'}

        new_cert_values = dict(self.cert_values)
        new_cert_values.update({'changed_by': 'system'})
        self.created_certificate_user = CertificateUser.objects.create(**new_cert_values)
        self.cert_values.update({'certificate_years_valid': CertificateUserConstants.CERTIFICATE_YEARS_VALID_1})

    def test_certficate_user_edit(self):
        new_cert_values = dict(self.cert_values)
        new_cert_values['name'] = 'old test certificate'
        new_cert_values['status'] = 2
        new_cert_values['action'] = '_save'

        resp = self.client.post(reverse('dafousers:certificateuser-edit', args=[self.created_certificate_user.id]), new_cert_values, follow=True)
        self.assertRedirects(resp, reverse('dafousers:certificateuser-list'))
        changed_certificate = CertificateUser.objects.get(pk=self.created_certificate_user.pk)
        self.assertEqual(changed_certificate.name, new_cert_values['name'])
        self.assertEqual(changed_certificate.status, 2)

    def test_certificate_user_history(self):
        self.assertEqual(len(self.created_certificate_user.certificateuserhistory_set.all()), 0)
        self.test_certficate_user_edit()
        self.assertEqual(len(self.created_certificate_user.certificateuserhistory_set.all()), 1)
        resp = self.client.get(reverse('dafousers:certificateuser-history', args=[self.created_certificate_user.pk]))
        self.assertEqual(resp.status_code, 200)
        self.assertListEqual(list(resp.context['history']), list(self.created_certificate_user.certificateuserhistory_set.all()))

    def test_certificate_user_create(self):
        new_cert_values = dict(self.cert_values)
        new_cert_values.update({'action': '_save'})
        resp = self.client.post(reverse('dafousers:certificateuser-add'), new_cert_values, follow=True)
        self.assertRedirects(resp, reverse('dafousers:certificateuser-list'))

    def test_certificate_user_list(self):
        resp = self.client.get(reverse('dafousers:certificateuser-list'))
        self.assertEqual(200, resp.status_code)
        self.assertListEqual(list(resp.context['object_list']), [self.created_certificate_user])


class OrganizationIdentityProviderTestCase(LoggedIn):

    def setUp(self):
        super(OrganizationIdentityProviderTestCase, self).setUp()
        self.new_provider_account = {'name': 'test_account', 'idp_entity_id': 'test_idp', 'idp_type': 2,
                                     'contact_email': 'test@test.dk', 'status': 1,
                                     'userprofile_attribute_format': IdentityProviderAccountConstants.USERPROFILE_FORMAT_MULTIVALUE,
                                     'userprofile_adjustment_filter_type': IdentityProviderAccountConstants.USERPROFILE_FILTER_NONE,
                                     'changed_by': 'system'}

        self.created_provider_account = IdentityProviderAccount.objects.create(**self.new_provider_account)
        self.secondary_created_provider_account = IdentityProviderAccount.objects.create(**self.new_provider_account)
        self.new_provider_account.update({'action': '_save'})
        del self.new_provider_account['changed_by']

    def test_identity_provider_account_edit(self):
        updated_provider_values = dict(self.new_provider_account)
        updated_provider_values['name'] = 'updated provider'
        resp = self.client.post(reverse('dafousers:identityprovideraccount-edit', args=[self.created_provider_account.pk]), updated_provider_values, follow=True)
        self.assertRedirects(resp, reverse('dafousers:identityprovideraccount-list'))
        updated_provider = IdentityProviderAccount.objects.get(pk=self.created_provider_account.pk)
        self.assertEqual(updated_provider.name, updated_provider_values['name'])
        self.assertEqual(updated_provider.changed_by, 'jakob@data.nanoq.gl')

    def test_identity_provider_account_history(self):
        self.assertEqual(len(self.created_provider_account.identityprovideraccounthistory_set.all()), 0)
        self.test_identity_provider_account_edit()
        self.assertEqual(len(self.created_provider_account.identityprovideraccounthistory_set.all()), 1)
        resp = self.client.get(reverse('dafousers:identityprovideraccount-history', args=[self.created_provider_account.pk]))
        self.assertEqual(resp.status_code, 200)
        self.assertListEqual(list(resp.context['history']), list(self.created_provider_account.identityprovideraccounthistory_set.all()))

    def test_identity_provider_account_create(self):
        self.assertEqual(IdentityProviderAccount.objects.count(), 2)
        resp = self.client.post(reverse('dafousers:identityprovideraccount-add'), self.new_provider_account, follow=True)
        self.assertRedirects(resp, reverse('dafousers:identityprovideraccount-list'))
        self.assertEqual(IdentityProviderAccount.objects.count(), 3)

    def test_identity_provider_account_list(self):
        resp = self.client.get(reverse('dafousers:identityprovideraccount-list'))
        self.assertEqual(200, resp.status_code)
        self.assertListEqual(list(resp.context['object_list']), [self.created_provider_account,
                                                                 self.secondary_created_provider_account])

    def test_ajax_identity_provider_account_queryset(self):
        resp = self.client.get(reverse('dafousers:update_identityprovideraccount_queryset'))
        self.assertEqual(resp.status_code, 200)
        self.assertEqual(resp.context['object_list'][0], self.created_provider_account)

    def test_ajax_identity_provider_account_status(self):
        resp = self.client.post(reverse('dafousers:update_identityprovideraccount'), {'action': 'ad_status_3',
                                                                           'user_id': [self.created_provider_account.id,
                                                                                       self.secondary_created_provider_account.id]})
        self.assertEqual(resp.status_code, 200)
        # both users should have been updated now
        first_change_account = IdentityProviderAccount.objects.get(id=self.created_provider_account.id)
        self.assertEqual(first_change_account.status, 3)
        self.assertEqual(first_change_account.changed_by, 'jakob@data.nanoq.gl')
        second_change_account = IdentityProviderAccount.objects.get(id=self.secondary_created_provider_account.id)
        self.assertEqual(second_change_account.status, 3)
        self.assertEqual(second_change_account.changed_by, 'jakob@data.nanoq.gl')


class UserProfileTestCase(LoggedIn):
    def setUp(self):
        super(UserProfileTestCase, self).setUp()
        self.profile = UserProfile.objects.create(name='test_object', changed_by='system')
        self.profile2 = UserProfile.objects.create(name='test_object2', changed_by='system')

    def test_user_profile_edit(self):
        resp = self.client.post(reverse('dafousers:userprofile-edit', args=[self.profile.pk]), {'name': 'new name'})
        self.assertRedirects(resp, reverse('dafousers:userprofile-list'))
        profile = UserProfile.objects.get(pk=self.profile.pk)
        self.assertEqual(profile.name, 'new name')

    def test_user_profile_history(self):
        self.assertEqual(len(self.profile.userprofilehistory_set.all()), 0)
        self.test_user_profile_edit()
        self.assertEqual(len(self.profile.userprofilehistory_set.all()), 1)
        resp = self.client.get(reverse('dafousers:userprofile-history', args=[self.profile.pk]))
        self.assertEqual(resp.status_code, 200)
        self.assertListEqual(list(resp.context['history']), list(self.profile.userprofilehistory_set.all()))

    def test_user_profile_add(self):
        self.assertEqual(UserProfile.objects.count(), 4)
        resp = self.client.post(reverse('dafousers:userprofile-add'), {'name': 'new_profile', 'action': '_save'}, follow=True)
        self.assertRedirects(resp, reverse('dafousers:userprofile-list'))
        self.assertEqual(UserProfile.objects.count(), 5)

    def test_user_profile_list(self):
        resp = self.client.get(reverse('dafousers:userprofile-list'))
        self.assertEqual(200, resp.status_code)
        self.assertListEqual(list(UserProfile.objects.all()), list(resp.context['object_list']))