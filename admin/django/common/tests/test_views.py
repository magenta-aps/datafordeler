from django.urls import reverse
from django.test import TestCase, Client
from dafousers.models import PasswordUser, UserProfile


class ViewsTestCase(TestCase):
    def setUp(self):
        self.client = Client()

    def test_index(self):
        resp = self.client.get('/', follow=True)
        self.assertEqual(resp.status_code, 200)

    def test_index_redirect_frontpage(self):
        user = PasswordUser.objects.create(email='test@test.dk', givenname='test', changed_by='system')
        user.password_salt, user.encrypted_password = PasswordUser.generate_encrypted_password_and_salt('test')
        user.save()
        profile = UserProfile.objects.get(name='DAFO Administrator')
        user.user_profiles.add(profile)
        logged_in = self.client.login(**{'username': 'test@test.dk', 'password': 'test'})
        self.assertTrue(logged_in)
        resp = self.client.get('/', follow=True)  # should result in a redirect...
        self.assertRedirects(resp, reverse("common:frontpage"), status_code=302, target_status_code=200)
        self.client.logout()

    def test_frontpage_not_logged_in(self):
        resp = self.client.get(reverse('common:frontpage'))
        self.assertEqual(resp.status_code, 302)

    def test_frontpage(self):
        self.client.login(**{'username': 'jakob@data.nanoq.gl', 'password': 'jacob'})
        resp = self.client.get(reverse('common:frontpage'))
        self.assertEqual(resp.status_code, 200)
        self.client.logout()

    def test_login_user_password(self):
        resp = self.client.post(reverse('common:login'), {'username': 'jakob@data.nanoq.gl', 'password': 'jacob'}, follow=True)
        self.assertEqual(resp.status_code, 200)
        self.assertTrue(resp.context['user'].is_authenticated)

    def test_login_user_password_fail(self):
        resp = self.client.post(reverse('common:login'), {'username': 'jako@data.nanoq.gl', 'password': 'jacob'}, follow=True)
        self.assertEqual(resp.status_code, 200)
        self.assertFalse(resp.context['user'].is_authenticated)

    def test_logout(self):
        self.client.login(**{'username': 'jakob@data.nanoq.gl', 'password': 'jacob'})
        resp = self.client.post(reverse('common:logout'), follow=True)
        self.assertFalse(resp.context['user'].is_authenticated)

    def test_js_catalog(self):
        resp = self.client.get(reverse('common:javascript-catalog'))
        self.assertEqual(resp.status_code, 200)

    def test_rst_doc_view(self):
        resp = self.client.get(reverse('common:doc', args=['access-rights']))
        self.assertEqual(resp.status_code, 200)