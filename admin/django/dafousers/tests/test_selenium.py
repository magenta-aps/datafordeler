# -*- coding: utf-8 -*-

from __future__ import absolute_import, unicode_literals, print_function
from dafousers import models, model_constants
from django import test
from django.conf import settings
from django.core.exceptions import FieldDoesNotExist
from django.core.management import call_command
from django.db.models.fields.files import FieldFile
from django.db.models.fields.related import ManyToManyField
from django.test import tag
from django.test import override_settings

import contextlib
import copy
import datetime
import logging
import os
import platform
import shutil
import time
import unittest
import uuid

try:
    import selenium.webdriver
except ImportError:
    selenium = None


logging.basicConfig(filename = "../logs/test.log", level = logging.DEBUG)
DUMMY_DOMAIN = 'http://localhost'


class CodeStyleTests(test.SimpleTestCase):

    @property
    def rootdir(self):
        return os.path.dirname(os.path.dirname(__file__))

    @property
    def source_files(self):
        """Generator that yields Python sources to test"""

        for dirpath, dirs, fns in os.walk(self.rootdir):
            dirs[:] = [
                dn for dn in dirs
                if dn != 'migrations' and not dn.startswith('venv-')
            ]

            for fn in fns:
                if fn[0] != '.' and fn.endswith('.py'):
                    yield os.path.join(dirpath, fn)

    # @tag('pep8')
    # def test_pep8(self):
    #     pep8style = pycodestyle.StyleGuide()
    #     pep8style.init_report(pycodestyle.StandardReport)
    #
    #     buf = io.StringIO()
    #
    #     with
    #         for fn in self.source_files:
    #             pep8style.check_files([fn])
    #
    #     assert not buf.getvalue(), \
    #         "Found code style errors and/or warnings:\n\n" + buf.getvalue()

    def test_source_files(self):
        sources = list(self.source_files)
        self.assert_(sources)
        self.assertGreater(len(sources), 1, sources)

    @staticmethod
    @contextlib.contextmanager
    def capture():
        import sys
        from cStringIO import StringIO
        oldout,olderr = sys.stdout, sys.stderr
        try:
            out=[StringIO(), StringIO()]
            sys.stdout,sys.stderr = out
            yield out
        finally:
            sys.stdout,sys.stderr = oldout, olderr
            out[0] = out[0].getvalue()
            out[1] = out[1].getvalue()


SAFARI_PREVIEW_DRIVER = ('/Applications/Safari Technology Preview.app'
                         '/Contents/MacOS/safaridriver')
CHROME_UBUNTU_DRIVER = '/usr/local/bin/chromedriver'

USERNAME = "jakob@data.nanoq.gl"
PASSWORD = "jacob"


@override_settings(DEBUG=True)
class BrowserTest(test.LiveServerTestCase):

    def setUp(self):
        from django import db
        db.connections.close_all()
        #kinda works because it clsoes the dirtry connection created in  test_bulk_status_change
        user_id, created = models.UserIdentification.objects.get_or_create(user_id='jakob@data.nanoq.gl', defaults={'user_id': 'jakob@data.nanoq.gl'})

        system_role, created = models.SystemRole.objects.get_or_create(role_name='DAFO Administrator',
                                                defaults={'role_name':'DAFO Administrator',
                                                          'role_type': model_constants.SystemRole.TYPE_CUSTOM,
                                                          'target_name': "DAFO Admin"})

        dafo_admin, created = models.UserProfile.objects.get_or_create(name='DAFO Administrator',
                                                                       defaults={'name': 'DAFO Administrator',
                                                                                 'changed_by': 'system'})

        if created is True:
            dafo_admin.system_roles.set([system_role])

        service, created = models.SystemRole.objects.get_or_create(role_name='DAFO Serviceudbyder',
                                                                   defaults={"role_name": "DAFO Serviceudbyder",
                                                                             "role_type": model_constants.SystemRole.TYPE_CUSTOM,
                                                                             "target_name": "DAFO Admin"})

        dafo_service, created = models.UserProfile.objects.get_or_create(name='DAFO Serviceudbyder',
                                                                         defaults={'name': 'DAFO Serviceudbyder',
                                                                                   'changed_by': 'system'})
        if created is True:
            dafo_service.system_roles.set([service])

        test_user, created = models.PasswordUser.objects.get_or_create(email=USERNAME, defaults={'changed_by': 'system',
                                                                                                 'identified_user': user_id })
        if created is True:
            test_user.password_salt, test_user.encrypted_password = test_user.generate_encrypted_password_and_salt('jacob')
            test_user.save()
            test_user.user_profiles.set([dafo_admin])

        test_user, created = models.PasswordUser.objects.get_or_create(email='amalie@serviceudbyder.gl', defaults={'changed_by': 'system',
                                                                                                 'identified_user': user_id })


    @classmethod
    def setUpClass(cls):
        call_command('collectstatic', verbosity=0, interactive=False)

        default_driver = (
            'Safari'
            if platform.system() == 'Darwin'
            else 'Chrome'
        )
        driver_name = os.environ.get('BROWSER', default_driver)

        driver = getattr(selenium.webdriver, driver_name, None)

        if not driver:
            raise unittest.SkipTest('$BROWSER unset or invalid')

        args = {}

        if driver_name == 'Safari' and os.path.isfile(SAFARI_PREVIEW_DRIVER):
            args.update(executable_path=SAFARI_PREVIEW_DRIVER)

        if driver_name == 'Chrome' and platform.dist()[0] == 'Ubuntu':
            args.update(executable_path=CHROME_UBUNTU_DRIVER)

        try:
            cls.browser = driver(**args)
            cls.browser.set_window_size(1920, 1080)

        except Exception as exc:
            print(exc)
            raise unittest.SkipTest(exc.args[0])

        super(BrowserTest, cls).setUpClass()


    @classmethod
    def tearDownClass(cls):
        super(BrowserTest, cls).tearDownClass()
        cls.browser.quit()

    def select_options(self, field, names, submit_id, id=None):
        if id is None:
            id = field
        select = self.browser.find_element_by_id(id)
        related_model = self.model._meta.get_field(field).related_model
        options = select.find_elements_by_tag_name('option')
        for option in options:
            labels = self.m2m_names_to_labels(related_model, names)
            for label in labels:
                if option.text.strip() in label:
                    self.click(option)
        add_button = self.browser.find_element_by_id(submit_id)
        self.click(add_button)

    def fill_in_form(self, submit_id=None, **kwargs):
        for field, value in kwargs.items():

            if field in ['user_profiles', 'system_roles', 'area_restrictions']:
                self.select_options(
                    field,
                    value,
                    'id_%s_add_link' % field,
                    'id_%s_from' % field
                )

            else:
                e = self.browser.find_element_by_id("id_" + field)

                if ((
                                    e.tag_name == 'input' and
                                    e.get_attribute('type') in ('text', 'password', 'number', 'email', 'file')
                    ) or (
                            e.tag_name == 'textarea'
                )):
                    e.clear()
                    e.send_keys(
                        value,
                    )
                elif e.tag_name == 'select':
                    options = e.find_elements_by_tag_name('option')

                    for option in options:
                        if option.text.strip() == value:
                            self.click(option)
                            break
                    else:
                        self.fail('{} not one of {}'.format(
                            value, [o.text for o in options]),
                        )
                elif (e.tag_name == 'input' and
                              e.get_attribute('type') in ('checkbox', 'radio')):
                    if value != e.is_selected():
                        self.click(e)
                else:
                    self.fail('unhandled input element (' + e.tag_name + '): ' +
                              e.get_attribute('outerHTML'))

        if submit_id is not None:
            submit = self.browser.find_element_by_id(submit_id)
        else:
            submit = self.browser.find_element_by_css_selector(
                "input[type=submit]",
            )

        self.click(submit)

    def click(self, element):
        self.browser.execute_script("arguments[0].scrollIntoView();", element)
        element.click()

    def setup(self):
        pass

    def get_object_name(self, params=None):
        if params is None:
            params = self.create_form_params
        return params['name']

    def get_bulk_add_permissions_params_updated(self):
        result = {}

        for field, value in self.create_form_params.items():
            if isinstance(value, list):
                result[field] = \
                    value + \
                    self.bulk_add_permissions_params[field]
        return result

    logged_in = False

    def logout(self):
        self.browser.delete_all_cookies()
        self.browser.get(self.live_server_url + '/logout')
        self.browser.delete_all_cookies()
        self.browser.get(self.live_server_url + '/login')

        self.assertNotEqual(
            self.live_server_url, self.browser.current_url, 'logout failed!'
        )

        # sanitity check the credentials
        self.client.logout()
        self.logged_in = False

    def login(self, user=USERNAME, password=PASSWORD, expected=True, force=False):
        print("BrowserTest.login(%s,%s,%s,%s)" % (user, password, expected, force))
        if force or not self.logged_in:
            # logout
            self.logout()

            login_status = self.client.login(username=user, password=password)

            self.assertEqual(login_status, expected,
                             'Unexpected login status (credentials)!')

            self.fill_in_form(username=user, password=password)
            if expected:
                self.assert_page('/frontpage/')
            else:
                self.assert_page('/login/')
            self.logged_in = True

    def element_text(self, elem_id):
        return self.browser.find_element_by_id(elem_id).text.strip().lower()

    def m2m_names_to_labels(self, related_model, names):
        result = []
        for name in names:
            result.append(str(related_model.objects.get(name=name)))
        return result

    def assert_page(self, expected_page):
        self.assertEqual(
            self.live_server_url + expected_page,
            self.browser.current_url,
            "expected: %s, got %s" % (self.live_server_url + expected_page, self.browser.current_url)
        )

    def assert_lists(self, expected_list, actual_list, list_name):
        for expected, actual in zip(expected_list, actual_list):
            if type(expected) == datetime.datetime:
                expected = expected.strftime("%Y-%m-%d")
                try:
                    assert expected in actual
                except AssertionError as e:
                    raise(
                        AssertionError(
                            "%s %s doesn't contain what is expected %s." % (list_name, actual, expected)
                        )
                    )

            else:
                self.assertEqual(
                    expected,
                    actual,
                    "%s %s doesn't match what is expected %s." % (list_name, actual, expected)
                )


@override_settings(DEBUG=True)
@tag('selenium')
@unittest.skipIf(not selenium, 'selenium not installed')
class LoginTest(BrowserTest):

    def test_login(self):
        self.login(force=True)
        self.login(user="bogus", password="fail", expected=False, force=True)


class CrudTestMixin(object):

    frontpage = '/frontpage/'
    create_page = 'add/'
    list_page = 'list/'
    edit_page = '%d/'
    history_page = '%d/history/'

    files = []

    def m2m_label_to_id(self, related_model, names):
        result = []
        for name in names:
            result.append(related_model.objects.get(name=name).pk)
        return result

    def choice_label_to_id(self, choices, label):
        for (id, choice_label) in choices:
            if choice_label == label:
                return id

    def choice_id_to_label(self, choices, id):
        for (choice_id, label) in choices:
            if choice_id == id:
                return label

    def convert_form_params(self, params):
        out = {}
        for (key, value) in params.items():
            try:
                field = self.model._meta.get_field(key)
                if field.related_model is not None:
                    value = self.m2m_label_to_id(field.related_model, value)
                if field.choices is not None and len(field.choices) > 0:
                    value = self.choice_label_to_id(field.choices, value)
                out[key] = value
            except FieldDoesNotExist:
                if key != u'certificate_years_valid':
                    print("The field %s does not exist." % key)
        return out

    def compare_result(self, created_object, params):
        for (key, expected) in self.convert_form_params(params).items():
            actual = getattr(created_object, key)
            field = self.model._meta.get_field(key)
            if isinstance(actual, FieldFile):
                continue
            if isinstance(field, ManyToManyField):
                actual = [related.pk for related in actual.all()]
            self.assertEqual(expected, actual)

    def get_row_with_name(self, rows, name):
        list = self.get_rows_with_names(rows, [name])
        return list[0]

    def get_rows_with_names(self, rows, names):
        result = []
        for row in rows:
            elements = row.find_elements_by_class_name('txtdata')
            if len(elements) > 0:
                if elements[0].text in names:
                    result.append(row)
        return result

    def create_test_object(self, form_params):

        for file in self.files:
            shutil.copy(file, settings.MEDIA_ROOT)

        create_params = {'changed_by': USERNAME}
        create_params.update(self.convert_form_params(form_params))

        for (key, value) in create_params.items():
            if value in self.files:
                create_params[key] = settings.MEDIA_ROOT + os.sep + value[value.rindex(os.sep):]

        # First save the object and then m2m fields
        singleton_params = {
            key: value for key, value in create_params.items()
            if not isinstance(self.model._meta.get_field(key), ManyToManyField)
        }
        m2m_params = {
            key: value for key, value in create_params.items()
            if isinstance(self.model._meta.get_field(key), ManyToManyField)
        }

        created_object = self.model(**singleton_params)
        if self.model == models.IdentityProviderAccount:
            created_object.save_metadata()

        created_object.save()

        for key, value in m2m_params.items():
            m2m_field = None
            m2m_values = None
            if key == 'user_profiles':
                m2m_field = created_object.user_profiles
                m2m_values = models.UserProfile.objects.filter(pk__in=value)
            elif key == 'system_roles':
                m2m_field = created_object.system_roles
                m2m_values = models.SystemRole.objects.filter(pk__in=value)
            elif key == 'area_restrictions':
                m2m_field = created_object.area_restrictions
                m2m_values = models.AreaRestriction.objects.filter(pk__in=value)
            m2m_field.add(*m2m_values)

            # If there is a certificate years valid field then create a certificate
            if 'certificate_years_valid' in form_params.keys():
                created_object.create_certificate(self.certificate_years)

        created_object.save()

        return created_object

    def assert_update_success(self, original, changes):
        updated_params = dict(original)
        updated_params.update(changes)
        updated_object_name = self.get_object_name(updated_params)
        created_object = self.model.objects.search(updated_object_name).first()
        self.compare_result(created_object, updated_params)

    def click_object_in_list(self, rows, id, expected_values):
        object_row = self.get_row_with_name(rows, self.get_object_name())
        actual_values = [element.text for element in object_row.find_elements_by_class_name('txtdata')]
        if expected_values is not None:
            self.assert_lists(expected_values, actual_values, "List data")
        link = object_row.find_elements_by_css_selector('.txtdata>a')[0]
        self.click(link)
        self.assert_page((self.page + self.edit_page) % id)

    def test_create(self):
        print("%s.test_create" % self.__class__.__name__)
        self.login()

        self.setup()

        self.browser.get(self.live_server_url + self.frontpage)
        create_button = self.browser.find_element_by_id(
            self.create_button_id
        )
        self.click(create_button)
        self.assert_page(self.page + self.create_page)

        self.fill_in_form('submit_save', **self.create_form_params)

        self.assert_page(self.page + self.list_page)

        created_object = self.model.objects.search(self.get_object_name()).first()

        self.assertIsNotNone(created_object)
        self.compare_result(created_object, self.create_form_params)

        rows = self.browser.find_elements_by_css_selector(".update_%s_queryset_body>table>tbody>tr" % self.base_name)



        self.assertEqual(
            self.number_of_original_objects + 1,
            len(rows) - 1,
            'must contain %s rows, however %s found' % (self.number_of_original_objects + 1, len(rows) - 1)
        )
        actual_titles = [element.text for element in rows[0].find_elements_by_class_name('ordering')]
        self.assert_lists(self.expected_titles, actual_titles, "List headers")
        object_row = self.get_row_with_name(rows, self.get_object_name())
        actual_values = [element.text for element in object_row.find_elements_by_class_name('txtdata')]
        self.assert_lists(self.expected_values, actual_values, "List data")

    def test_edit(self):
        print("%s.test_edit" % self.__class__.__name__)
        self.login()
        self.setup()

        created_object = self.create_test_object(self.create_form_params)

        # Test that the item exists in the item list
        rows = self.assert_new_object_in_list(1)

        # Click the new object
        self.click_object_in_list(rows, created_object.pk, self.expected_values)

        if self.model == models.UserProfile:
            search_field_id = 'search_user_profile'
        else:
            search_field_id = 'search_org_user_system'

        # Test that the item can be searched
        self.browser.get(self.live_server_url + self.frontpage)
        search_field = self.browser.find_element_by_id(search_field_id)
        search_field.clear()
        search_field.send_keys(self.get_object_name()[0:6])
        time.sleep(1)

        result_list = self.browser.find_elements_by_css_selector('#%s_results>a' % search_field_id)
        result_link = None
        sought_text = "%s: %s" % (self.model._meta.verbose_name.title(), self.get_object_name())
        for link in result_list:
            if link.text == sought_text:
                result_link = link
                break

        self.assertIsNotNone(result_link)

        self.click(result_link)
        time.sleep(1)
        self.assert_page((self.page + self.edit_page) % created_object.pk)

        # Edit the form
        self.fill_in_form('submit_save', **self.edit_form_params)
        self.assert_page(self.page + self.list_page)

        self.assert_update_success(
            self.create_form_params,
            self.edit_form_params
        )

    def test_history(self):

        print("%s.test_history" % self.__class__.__name__)
        self.login()
        self.setup()

        created_object = self.create_test_object(self.create_form_params)

        # Test that the item exists in the item list
        rows = self.assert_new_object_in_list(1)

        # Click the new object
        self.click_object_in_list(rows, created_object.pk, self.expected_values)

        # Edit the form
        self.fill_in_form('submit_save', **self.edit_form_params)
        self.assert_page(self.page + self.list_page)

        self.assert_update_success(
            self.create_form_params,
            self.edit_form_params
        )

        # Get updated rows
        rows = self.assert_new_object_in_list(1)

        # Click the new object
        self.click_object_in_list(rows, created_object.pk, None)

        # Go to history page
        link = self.browser.find_element_by_id('goto_history')
        self.click(link)
        self.assert_page((self.page + self.history_page) % created_object.pk)

        # Get reversed entry list
        entries = list(
            reversed(
                self.browser.find_elements_by_css_selector('.history-entry')
            )
        )

        expected_history_entries = 2

        self.assertEqual(
            expected_history_entries,
            len(entries),
            '%s history entries were expected, however, %s was found' % (expected_history_entries, len(entries))
        )

        for i in range(2):
            for param in entries[i].find_elements_by_css_selector('.history-entry-content>.row.row_title'):
                param_parts = param.find_elements_by_class_name('row_field')
                param_title = param_parts[0].text
                param_value = param_parts[1].text

                if i == 0:
                    expected_items = self.create_form_params
                else:
                    expected_items = dict(self.create_form_params)
                    expected_items.update(self.edit_form_params)


                expected_params = {}
                for key, value in expected_items.items():
                    try:
                        field = self.model._meta.get_field(key)
                        if isinstance(field, ManyToManyField):
                            related_model = field.related_model
                            value = self.m2m_names_to_labels(related_model, value)
                            value = "\n".join(value)
                        expected_params[field.verbose_name] = value
                    except FieldDoesNotExist:
                        if key != u'certificate_years_valid':
                            print("The field %s does not exist." % key)
                for expected_title, expected_value in expected_params.items():
                    if param_title == expected_title:
                        self.assertEqual(
                            expected_value,
                            param_value,
                            "expected the historical value for %s in history entry %s to be %s, however, it was %s" %
                            (param_title, i, expected_value, param_value)
                        )


    def select_permissions_and_submit(self, permission_type):
        add_permissions = self.browser.find_element_by_id('add_%s' % permission_type)
        if permission_type in self.bulk_add_permissions_params:
            self.click(add_permissions)
            names = self.bulk_add_permissions_params[permission_type]
            self.select_options(
                permission_type,
                names,
                'submit_%s_popup' % permission_type
            )

    def change_status(self, status_name):
        change_status = self.browser.find_element_by_id('change_status')
        self.click(change_status)
        status_list = self.browser.find_element_by_id('status')
        buttons = status_list.find_elements_by_tag_name('button')
        for button in buttons:
                if button.text.strip() == status_name:
                    self.click(button)
                    time.sleep(1)

    def create_test_objects(self, iterations):

        create_objects = []
        create_objects_params = []
        for i in range(iterations):
            object_params = copy.copy(self.create_form_params)
            name_key = None
            if 'name' in object_params:
                name_key = 'name'
            elif 'lastname' in object_params:
                name_key = 'lastname'
            if name_key is not None:
                object_params[name_key] += " %s" % i

            email_key = None
            if 'email' in object_params:
                email_key = 'email'
            elif 'contact_email' in object_params:
                email_key = 'contact_email'
            if email_key is not None:
                object_params[email_key] += " %s" % i

            object = self.create_test_object(object_params)
            create_objects.append(object)
            create_objects_params.append(object_params)

        return create_objects, create_objects_params

    def assert_new_object_in_list(self, iterations):
        # Test that the item exists in the item list
        self.browser.get(self.live_server_url + self.page + self.list_page)
        rows = self.browser.find_elements_by_css_selector(".update_%s_queryset_body>table>tbody>tr" % self.base_name)
        self.assertEqual(
            self.number_of_original_objects + iterations,
            len(rows) - 1,
            'must contain %s rows, however %s found' % (self.number_of_original_objects, len(rows) - 1)
        )
        return rows

    def select_table_items(self, params_to_update, rows):
        for params in params_to_update:
            object_row = self.get_row_with_name(
                rows,
                self.get_object_name(params)
            )
            checkbox = object_row.find_element_by_css_selector(
                'input[type=checkbox]',
            )
            self.click(checkbox)

    def create_objects_and_select_them(self):
        iterations = 10
        create_objects, create_objects_params = self.create_test_objects(iterations)
        rows = self.assert_new_object_in_list(iterations)

        pks_to_update = [2, 3, 5, 7]
        params_to_update = [create_objects_params[index] for index in pks_to_update]
        self.select_table_items(params_to_update, rows)
        return params_to_update

    def test_bulk_add_permissions(self):
        print("%s.test_bulk_add_permissions" % self.__class__.__name__)
        self.login()
        self.setup()

        # Create test objects and select them so that they are ready for bulk action
        params_to_update = self.create_objects_and_select_them()

        if self.model == models.UserProfile:
            self.select_permissions_and_submit('system_roles')
            self.select_permissions_and_submit('area_restrictions')
        else:
            self.select_permissions_and_submit('user_profiles')

        time.sleep(1)

        for params in params_to_update:
            self.assert_update_success(
                params,
                self.get_bulk_add_permissions_params_updated()
            )

    def test_bulk_status_change(self):
        if hasattr(self.model, 'status'):

            print("%s.test_bulk_status_change" % self.__class__.__name__)
            self.login()
            self.setup()
            # Create test objects and select them so that they are ready for bulk action
            params_to_update = self.create_objects_and_select_them()

            expected_status = u'Deaktiveret'
            self.change_status(expected_status)
            
            time.sleep(1)


            for params in params_to_update:
                self.assert_update_success(
                    params,
                    {'status': expected_status}
                )


@override_settings(DEBUG=True)
@tag('selenium')
@unittest.skipIf(not selenium, 'selenium not installed')
class IdentityProviderAccountTest(CrudTestMixin, BrowserTest):

    def __init__(self, *args, **kwargs):
        super(IdentityProviderAccountTest, self).__init__(*args, **kwargs)
        self.model = models.IdentityProviderAccount
        self.base_name = 'identityprovideraccount'
        self.create_button_id = 'create_identityprovider_account'
        self.page = '/organisation/'

        resource_path = os.path.abspath(os.getcwd() + "/../testresources")
        self.files = [
            resource_path + "/test_metadata.xml"
        ]

        self.create_form_params = {
            'name': 'Magenta ApS',
            'contact_name': 'Peter Rasmussen',
            'contact_email': 'pera@magenta.dk',
            'idp_type': u'Primær IdP (ingen validering af brugerprofiler)',
            'metadata_xml_file': resource_path + "/test_metadata.xml",
            'userprofile_attribute': 'urn:user_profiles',
            'userprofile_attribute_format': 'Kommasepareret liste',
            'userprofile_adjustment_filter_type': 'Ingen tilpasninger',
            'organisation': u'Magenta leverer Grønlands datafordeler',
            'status': u'Aktiv',
            'user_profiles': [u'DAFO Serviceudbyder']
        }

        self.edit_form_params = {
            'idp_type': u'Sekundær IdP (kan kun udstede angivne brugerprofiler)',
            'metadata_xml_file': resource_path + "/test_metadata.xml"
        }

        self.bulk_add_permissions_params = {
            'user_profiles': [u'Magenta CPRBroker']
        }

        self.expected_titles = [
            u'Navn',
            u'Navn på kontaktperson',
            u'E-mailadresse på kontaktperson',
            u'IdP type',
            u'IdP Entity ID',
            u'Status'
        ]

        self.expected_values = [
            self.get_object_name(),
            self.create_form_params['contact_name'],
            self.create_form_params['contact_email'],
            self.create_form_params['idp_type'],
            u'https://accounts.google.com/o/saml2?idpid=foobar',
            self.create_form_params['status']
        ]

        self.number_of_original_objects = 0

    def setup(self):
        user_profile = models.UserProfile(
            name=u'Magenta CPRBroker',
            changed_by="admin"
        )
        user_profile.save()

@override_settings(DEBUG=True)
@tag('selenium')
@unittest.skipIf(not selenium, 'selenium not installed')
class PasswordUserTest(CrudTestMixin, BrowserTest):

    def __init__(self, *args, **kwargs):
        super(PasswordUserTest, self).__init__(*args, **kwargs)
        self.model = models.PasswordUser
        self.base_name = 'passworduser'
        self.create_button_id = 'create_password_user'
        self.page = '/user/'

        self.create_form_params = {
            'givenname': u'Peter',
            'lastname': u'Rasmussen',
            'email': u'pera@magenta.dk',
            'organisation': u'Magenta ApS',
            'status': u'Aktiv',
            'user_profiles': [u'DAFO Serviceudbyder']
        }

        self.edit_form_params = {
            'email': u'pera@magenta2.dk',
            'organisation': u'Magenta 2 ApS'
        }

        self.bulk_add_permissions_params = {
            'user_profiles': [u'Magenta CPRBroker']
        }

        self.expected_titles = [
            u'Bruger',
            u'Email',
            u'Arbejdssted',
            u'Status'
        ]

        self.expected_values = [
            self.get_object_name(),
            self.create_form_params['email'],
            self.create_form_params['organisation'],
            self.create_form_params['status']
        ]

        self.number_of_original_objects = 2

    def setup(self):
        user_profile = models.UserProfile(
            name=u'Magenta CPRBroker',
            changed_by="admin"
        )
        user_profile.save()

    def get_object_name(self, params=None):
        if params is None:
            params = self.create_form_params
        return params['givenname'] + " " + params['lastname']


@override_settings(DEBUG=True)
@tag('selenium')
@unittest.skipIf(not selenium, 'selenium not installed')
class CertificateUserTest(CrudTestMixin, BrowserTest):

    def __init__(self, *args, **kwargs):
        super(CertificateUserTest, self).__init__(*args, **kwargs)
        self.model = models.CertificateUser
        self.base_name = 'certificateuser'
        self.create_button_id = 'create_certificate_user'
        self.page = '/system/'

        self.create_form_params = {
            'name': u'Magenta ApS',
            'identification_mode': u'Identificerer brugere via \'på-vegne-af\'',
            'contact_name': u'Peter Rasmussen',
            'contact_email': u'pera@magenta.dk',
            'certificate_years_valid': u'3 år',
            'organisation': u'Magenta leverer Grønlands Datafordeler',
            'comment': u'I følge aftale.',
            'status': u'Aktiv',
            'user_profiles': [u'DAFO Serviceudbyder']
        }

        self.edit_form_params = {
        }

        self.bulk_add_permissions_params = {
            'user_profiles': [u'Magenta CPRBroker']
        }

        self.edit_form_action = 'delete_invalid_certificates'

        self.expected_titles = [
            u'Navn',
            u'Næste udløbsdato',
            u'Identifikationsmetode',
            u'Navn på kontaktperson',
            u'E-mailadresse på kontaktperson',
            u'Status'
        ]

        d = datetime.datetime.now()
        self.certificate_years = 3
        d = d + datetime.timedelta(days=(self.certificate_years*365))

        self.expected_values = [
            self.get_object_name(),
            d,
            self.create_form_params['identification_mode'],
            self.create_form_params['contact_name'],
            self.create_form_params['contact_email'],
            self.create_form_params['status']
        ]

        self.number_of_original_objects = 0

    def setup(self):
        user_profile = models.UserProfile(
            name=u'Magenta CPRBroker',
            changed_by="admin"
        )
        user_profile.save()


@override_settings(DEBUG=True)
@tag('selenium')
@unittest.skipIf(not selenium, 'selenium not installed')
class UserProfileTest(CrudTestMixin, BrowserTest):

    def __init__(self, *args, **kwargs):
        super(UserProfileTest, self).__init__(*args, **kwargs)
        self.model = models.UserProfile
        self.base_name = 'userprofile'
        self.create_button_id = 'create_user_profile'
        self.page = '/user-profile/'

        self.files = []

        self.create_form_params = {
            'name': u'Magenta CPRBroker',
            'area_restrictions': [u'Vest']
        }

        self.edit_form_params = {
            'area_restrictions': [u'Vest', u'Syd']
        }

        self.bulk_add_permissions_params = {
            'area_restrictions': [u'Syd']
        }

        self.expected_titles = [
            u'Navn',
        ]

        self.expected_values = [
            self.get_object_name()
        ]

        self.number_of_original_objects = 2

    def setup(self):
        municipality_type = models.AreaRestrictionType(name=u'Kommune')
        municipality_type.save()
        municipality_west = models.AreaRestriction(
            name=u'Vest',
            area_restriction_type=municipality_type,
            sumiffiik=str(uuid.uuid4())
        )
        municipality_south = models.AreaRestriction(
            name=u'Syd',
            area_restriction_type=municipality_type,
            sumiffiik=str(uuid.uuid4())
        )
        municipality_west.save()
        municipality_south.save()
